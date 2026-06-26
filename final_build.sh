#!/bin/bash

export JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2
export PATH=$JAVA_HOME/bin:$PATH
export MAVEN_HOME=/opt/apache-maven-3.9.6
export PATH=$MAVEN_HOME/bin:$PATH

REPO_URL="https://maven.aliyun.com/repository/public"
LOCAL_REPO="/root/.m2/repository"
PROJECT_DIR="/workspace/credit-platform"

download_pom() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    
    local group_path=$(echo $group_id | tr '.' '/')
    local dir="$LOCAL_REPO/$group_path/$artifact_id/$version"
    local file="$dir/$artifact_id-$version.pom"
    
    if [ -f "$file" ] && [ -s "$file" ]; then
        return 0
    fi
    
    mkdir -p "$dir"
    
    local url="$REPO_URL/$group_path/$artifact_id/$version/$artifact_id-$version.pom"
    
    if wget -q --timeout=30 --tries=3 -O "$file" "$url" 2>/dev/null; then
        rm -f "$file.lastUpdated"
        echo "POM: $group_id:$artifact_id:$version"
        return 0
    else
        rm -f "$file"
        return 1
    fi
}

download_jar() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    
    local group_path=$(echo $group_id | tr '.' '/')
    local dir="$LOCAL_REPO/$group_path/$artifact_id/$version"
    local file="$dir/$artifact_id-$version.jar"
    
    if [ -f "$file" ] && [ -s "$file" ]; then
        return 0
    fi
    
    mkdir -p "$dir"
    
    local url="$REPO_URL/$group_path/$artifact_id/$version/$artifact_id-$version.jar"
    
    if wget -q --timeout=30 --tries=3 -O "$file" "$url" 2>/dev/null; then
        rm -f "$file.lastUpdated"
        echo "JAR: $group_id:$artifact_id:$version"
        return 0
    else
        rm -f "$file"
        return 1
    fi
}

# 递归下载 POM 的所有父 POM
download_parent_poms() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return
    fi
    
    local parent_group=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<groupId>" | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | xargs)
    local parent_artifact=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<artifactId>" | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | xargs)
    local parent_version=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<version>" | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | xargs)
    
    if [ -n "$parent_group" ] && [ -n "$parent_artifact" ] && [ -n "$parent_version" ] && ! [[ "$parent_version" =~ \$\{ ]]; then
        if download_pom "$parent_group" "$parent_artifact" "$parent_version"; then
            local group_path=$(echo $parent_group | tr '.' '/')
            local parent_pom="$LOCAL_REPO/$group_path/$parent_artifact/$parent_version/$parent_artifact-$parent_version.pom"
            download_parent_poms "$parent_pom"
        fi
    fi
}

cd "$PROJECT_DIR"

# 清理 lastUpdated
find "$LOCAL_REPO" -name "*.lastUpdated" -delete

echo "=== 阶段 1: 递归下载所有 POM 的父 POM ==="
for pom in $(find "$LOCAL_REPO" -name "*.pom" -type f); do
    download_parent_poms "$pom"
done

echo ""
echo "=== 阶段 2: 尝试编译，逐批下载缺失的依赖 ==="

for attempt in $(seq 1 50); do
    echo ""
    echo "--- 第 $attempt 次编译尝试 ---"
    
    mvn compile -o -DskipTests 2>&1 > /tmp/mvn.log
    EXIT_CODE=$?
    
    if [ $EXIT_CODE -eq 0 ]; then
        echo ""
        echo "=========================================="
        echo "🎉 编译成功！"
        echo "=========================================="
        exit 0
    fi
    
    # 从日志中提取缺失的 artifacts
    missing=$(grep -oP 'The following artifacts could not be resolved: \K[^ ]+' /tmp/mvn.log 2>/dev/null | tr ',' '\n' | sed 's/^[[:space:]]*//' | sort -u)
    
    if [ -z "$missing" ]; then
        # 尝试另一种模式
        missing=$(grep -oP 'Cannot access.*artifact \K[^ ]+' /tmp/mvn.log 2>/dev/null | sort -u)
    fi
    
    if [ -z "$missing" ]; then
        echo ""
        echo "无法提取缺失的依赖，可能是编译代码错误。"
        echo "最后 80 行输出："
        tail -80 /tmp/mvn.log
        exit 1
    fi
    
    echo "发现 $(echo "$missing" | wc -l) 个缺失的依赖，开始下载..."
    
    downloaded=0
    failed=0
    
    while IFS= read -r artifact; do
        # 解析 artifact 字符串，格式可能是: group:artifact:type:version 或 group:artifact:version
        g=$(echo "$artifact" | cut -d: -f1)
        a=$(echo "$artifact" | cut -d: -f2)
        t=$(echo "$artifact" | cut -d: -f3)
        v=$(echo "$artifact" | cut -d: -f4)
        
        # 如果只有 3 段，说明格式是 group:artifact:version，type 默认为 jar
        if [ -z "$v" ]; then
            v="$t"
            t="jar"
        fi
        
        if [ -z "$g" ] || [ -z "$a" ] || [ -z "$v" ]; then
            continue
        fi
        
        # 下载 POM
        if download_pom "$g" "$a" "$v"; then
            downloaded=$((downloaded + 1))
            # 递归下载父 POM
            group_path=$(echo $g | tr '.' '/')
            pom_file="$LOCAL_REPO/$group_path/$a/$v/$a-$v.pom"
            download_parent_poms "$pom_file"
        else
            failed=$((failed + 1))
        fi
        
        # 如果不是 pom 类型，也下载 JAR
        if [ "$t" != "pom" ]; then
            if download_jar "$g" "$a" "$v"; then
                downloaded=$((downloaded + 1))
            else
                failed=$((failed + 1))
            fi
        fi
    done <<< "$missing"
    
    echo "下载完成: 成功 $downloaded, 失败 $failed"
    
    if [ $downloaded -eq 0 ]; then
        echo "没有下载到任何依赖，退出。"
        tail -80 /tmp/mvn.log
        exit 1
    fi
    
    # 清理 lastUpdated
    find "$LOCAL_REPO" -name "*.lastUpdated" -delete
done

echo ""
echo "超过最大尝试次数"
exit 1
