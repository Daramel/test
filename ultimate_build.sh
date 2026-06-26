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
        echo "  POM: $group_id:$artifact_id:$version"
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
        echo "  JAR: $group_id:$artifact_id:$version"
        return 0
    else
        rm -f "$file"
        return 1
    fi
}

# 递归下载 POM 的父 POM 和 import POM
download_pom_deps() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return 1
    fi
    
    local changed=0
    
    # 下载父 POM
    local parent_group=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<groupId>" | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | xargs)
    local parent_artifact=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<artifactId>" | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | xargs)
    local parent_version=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<version>" | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | xargs)
    
    if [ -n "$parent_group" ] && [ -n "$parent_artifact" ] && [ -n "$parent_version" ] && ! [[ "$parent_version" =~ \$\{ ]]; then
        local group_path=$(echo $parent_group | tr '.' '/')
        local parent_pom="$LOCAL_REPO/$group_path/$parent_artifact/$parent_version/$parent_artifact-$parent_version.pom"
        
        if [ ! -f "$parent_pom" ] || [ ! -s "$parent_pom" ]; then
            if download_pom "$parent_group" "$parent_artifact" "$parent_version"; then
                changed=1
                download_pom_deps "$parent_pom"
            fi
        fi
    fi
    
    # 下载 scope=import 的 BOM
    while IFS= read -r block; do
        local ig=$(echo "$block" | grep -oP '<groupId>\K[^<]+' | head -1)
        local ia=$(echo "$block" | grep -oP '<artifactId>\K[^<]+' | head -1)
        local iv=$(echo "$block" | grep -oP '<version>\K[^<]+' | head -1)
        
        if [ -n "$ig" ] && [ -n "$ia" ] && [ -n "$iv" ] && ! [[ "$iv" =~ \$\{ ]]; then
            local gp=$(echo $ig | tr '.' '/')
            local ipom="$LOCAL_REPO/$gp/$ia/$iv/$ia-$iv.pom"
            
            if [ ! -f "$ipom" ] || [ ! -s "$ipom" ]; then
                if download_pom "$ig" "$ia" "$iv"; then
                    changed=1
                    download_pom_deps "$ipom"
                fi
            fi
        fi
    done < <(grep -B 2 -A 15 "<scope>import</scope>" "$pom_file" 2>/dev/null | awk '/<dependency>/ {buf=""} {buf=buf"\n"$0} /<\/dependency>/ {print buf; buf=""}')
    
    return $changed
}

cd "$PROJECT_DIR"

# 清理 lastUpdated
find "$LOCAL_REPO" -name "*.lastUpdated" -delete

echo "=== 终极自动构建脚本 ==="

for attempt in $(seq 1 100); do
    echo ""
    echo "=========================================="
    echo "第 $attempt 次编译尝试"
    echo "=========================================="
    
    # 运行离线编译，收集详细日志
    mvn compile -o -DskipTests -X 2>&1 > /tmp/mvn_full.log
    EXIT_CODE=$?
    
    if [ $EXIT_CODE -eq 0 ]; then
        echo ""
        echo "=========================================="
        echo "🎉 编译成功！"
        echo "=========================================="
        exit 0
    fi
    
    # 提取所有缺失的 artifacts
    echo "正在分析缺失的依赖..."
    
    # 从日志中提取所有 "could not be resolved" 的 artifacts
    missing=$(grep -oP 'The following artifacts could not be resolved: \K[^ ]+' /tmp/mvn_full.log 2>/dev/null | \
        tr ',' '\n' | \
        sed 's/^[[:space:]]*//' | \
        sed 's/ (absent).*$//' | \
        sort -u)
    
    # 也提取 "Cannot access.*artifact xxx" 的格式
    missing2=$(grep -oP 'artifact \K[^ ]+[^ ]+\.pom' /tmp/mvn_full.log 2>/dev/null | \
        sed 's/\.pom$//' | \
        sort -u)
    
    # 合并
    all_missing=$(printf "%s\n%s\n" "$missing" "$missing2" | sort -u | grep -v '^$')
    
    if [ -z "$all_missing" ]; then
        echo ""
        echo "没有找到缺失的依赖。检查是否是编译代码错误..."
        echo ""
        echo "=== 编译错误摘要 ==="
        grep -E "^\[ERROR\].*\.java:" /tmp/mvn_full.log | head -50
        
        if [ $(grep -c "^\[ERROR\].*\.java:" /tmp/mvn_full.log) -gt 0 ]; then
            echo ""
            echo "发现代码编译错误，需要修复源代码。"
        fi
        exit 1
    fi
    
    count=$(echo "$all_missing" | wc -l)
    echo "发现 $count 个缺失的 artifacts，开始下载..."
    
    downloaded=0
    failed=0
    
    while IFS= read -r artifact; do
        # 解析 artifact 格式: group:artifact:type:version 或 group:artifact:version
        g=$(echo "$artifact" | cut -d: -f1)
        a=$(echo "$artifact" | cut -d: -f2)
        t=$(echo "$artifact" | cut -d: -f3)
        v=$(echo "$artifact" | cut -d: -f4)
        
        if [ -z "$v" ]; then
            v="$t"
            t="jar"
        fi
        
        if [ -z "$g" ] || [ -z "$a" ] || [ -z "$v" ]; then
            continue
        fi
        
        # 跳过版本中包含变量的
        if [[ "$v" =~ \$\{ ]]; then
            continue
        fi
        
        # 下载 POM
        if download_pom "$g" "$a" "$v"; then
            downloaded=$((downloaded + 1))
            # 递归下载 POM 的依赖 POM
            group_path=$(echo $g | tr '.' '/')
            pom_file="$LOCAL_REPO/$group_path/$a/$v/$a-$v.pom"
            download_pom_deps "$pom_file" > /dev/null 2>&1
        else
            failed=$((failed + 1))
        fi
        
        # 如果是 JAR 类型，也下载 JAR
        if [ "$t" != "pom" ]; then
            if download_jar "$g" "$a" "$v"; then
                downloaded=$((downloaded + 1))
            else
                failed=$((failed + 1))
            fi
        fi
    done <<< "$all_missing"
    
    echo ""
    echo "下载完成: 成功 $downloaded, 失败 $failed"
    
    if [ $downloaded -eq 0 ]; then
        echo "没有下载到任何新依赖，退出。"
        exit 1
    fi
    
    # 清理 lastUpdated
    find "$LOCAL_REPO" -name "*.lastUpdated" -delete
done

echo ""
echo "超过最大尝试次数"
exit 1
