#!/bin/bash

export JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2
export PATH=$JAVA_HOME/bin:$PATH
export MAVEN_HOME=/opt/apache-maven-3.9.6
export PATH=$MAVEN_HOME/bin:$PATH

REPO_URL="https://maven.aliyun.com/repository/public"
LOCAL_REPO="/root/.m2/repository"
PROJECT_DIR="/workspace/credit-platform"

download_artifact() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    local packaging=${4:-jar}
    
    local group_path=$(echo $group_id | tr '.' '/')
    local dir="$LOCAL_REPO/$group_path/$artifact_id/$version"
    local file="$dir/$artifact_id-$version.$packaging"
    
    if [ -f "$file" ] && [ -s "$file" ]; then
        return 0
    fi
    
    mkdir -p "$dir"
    
    local url="$REPO_URL/$group_path/$artifact_id/$version/$artifact_id-$version.$packaging"
    
    if wget -q --timeout=30 --tries=3 -O "$file" "$url" 2>/dev/null; then
        rm -f "$file.lastUpdated"
        echo "下载: $group_id:$artifact_id:$version:$packaging"
        return 0
    else
        rm -f "$file"
        echo "失败: $group_id:$artifact_id:$version:$packaging"
        return 1
    fi
}

cd "$PROJECT_DIR"

# 清理 lastUpdated
find "$LOCAL_REPO" -name "*.lastUpdated" -delete

for attempt in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
    echo ""
    echo "=========================================="
    echo "第 $attempt 次尝试编译"
    echo "=========================================="
    
    # 运行离线编译
    mvn compile -o -DskipTests 2>&1 | tee /tmp/mvn_build.log
    
    EXIT_CODE=${PIPESTATUS[0]}
    
    if [ $EXIT_CODE -eq 0 ]; then
        echo ""
        echo "=========================================="
        echo "编译成功！"
        echo "=========================================="
        exit 0
    fi
    
    echo ""
    echo "编译失败，正在提取缺失的依赖..."
    
    # 从日志中提取缺失的依赖
    MISSING_ARTIFACTS=()
    
    # 提取插件缺失
    while IFS= read -r line; do
        if echo "$line" | grep -q "Plugin.*could not be resolved"; then
            # 提取插件 GAV
            plugin_info=$(echo "$line" | grep -oP 'Plugin \K[^ ]+' | head -1)
            if [ -n "$plugin_info" ]; then
                g=$(echo "$plugin_info" | cut -d: -f1)
                a=$(echo "$plugin_info" | cut -d: -f2)
                v=$(echo "$plugin_info" | cut -d: -f3)
                if [ -n "$g" ] && [ -n "$a" ] && [ -n "$v" ]; then
                    MISSING_ARTIFACTS+=("$g:$a:$v:pom")
                    MISSING_ARTIFACTS+=("$g:$a:$v:jar")
                fi
            fi
        fi
    done < /tmp/mvn_build.log
    
    # 提取 artifact 缺失
    while IFS= read -r line; do
        if echo "$line" | grep -q "Could not transfer artifact\|The following artifacts could not be resolved"; then
            # 提取 GAV
            artifact=$(echo "$line" | grep -oP '[a-zA-Z0-9_.\-]+:[a-zA-Z0-9_.\-]+:[a-zA-Z0-9_.\-]+' | head -1)
            if [ -n "$artifact" ]; then
                g=$(echo "$artifact" | cut -d: -f1)
                a=$(echo "$artifact" | cut -d: -f2)
                v=$(echo "$artifact" | cut -d: -f3)
                if [ -n "$g" ] && [ -n "$a" ] && [ -n "$v" ] && [ "$g" != "the" ]; then
                    # 检查是 pom 还是 jar
                    if echo "$line" | grep -q ":pom:"; then
                        MISSING_ARTIFACTS+=("$g:$a:$v:pom")
                    else
                        MISSING_ARTIFACTS+=("$g:$a:$v:pom")
                        MISSING_ARTIFACTS+=("$g:$a:$v:jar")
                    fi
                fi
            fi
        fi
    done < /tmp/mvn_build.log
    
    # 去重
    MISSING_ARTIFACTS=($(printf "%s\n" "${MISSING_ARTIFACTS[@]}" | sort -u))
    
    if [ ${#MISSING_ARTIFACTS[@]} -eq 0 ]; then
        echo ""
        echo "没有找到缺失的依赖，可能是编译错误而非依赖缺失。"
        echo "显示最后的 50 行错误："
        tail -50 /tmp/mvn_build.log
        exit 1
    fi
    
    echo ""
    echo "发现 ${#MISSING_ARTIFACTS[@]} 个缺失的 artifacts，开始下载..."
    
    success_count=0
    fail_count=0
    
    for artifact in "${MISSING_ARTIFACTS[@]}"; do
        g=$(echo "$artifact" | cut -d: -f1)
        a=$(echo "$artifact" | cut -d: -f2)
        v=$(echo "$artifact" | cut -d: -f3)
        p=$(echo "$artifact" | cut -d: -f4)
        
        if download_artifact "$g" "$a" "$v" "$p"; then
            success_count=$((success_count + 1))
        else
            fail_count=$((fail_count + 1))
        fi
    done
    
    echo ""
    echo "下载完成: 成功 $success_count, 失败 $fail_count"
    
    if [ $fail_count -gt 0 ] && [ $success_count -eq 0 ]; then
        echo "所有下载都失败了，网络可能有问题。"
        exit 1
    fi
    
    # 清理 lastUpdated
    find "$LOCAL_REPO" -name "*.lastUpdated" -delete
    
    echo "准备下一次编译..."
done

echo ""
echo "超过最大尝试次数，编译失败。"
exit 1
