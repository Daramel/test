#!/bin/bash
set -e

# Maven 仓库基础 URL
REPO_URL="https://maven.aliyun.com/repository/public"
LOCAL_REPO="/root/.m2/repository"

# 下载 POM 的函数
download_pom() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    
    local group_path=$(echo $group_id | tr '.' '/')
    local pom_dir="$LOCAL_REPO/$group_path/$artifact_id/$version"
    local pom_file="$pom_dir/$artifact_id-$version.pom"
    
    if [ -f "$pom_file" ] && [ -s "$pom_file" ]; then
        echo "已存在: $group_id:$artifact_id:$version"
        return 0
    fi
    
    mkdir -p "$pom_dir"
    
    local url="$REPO_URL/$group_path/$artifact_id/$version/$artifact_id-$version.pom"
    echo "下载: $url"
    
    wget -q --timeout=30 --tries=3 -O "$pom_file" "$url" 2>/dev/null || {
        echo "失败: $group_id:$artifact_id:$version"
        rm -f "$pom_file"
        return 1
    }
    
    # 清理 lastUpdated 文件
    rm -f "$pom_file.lastUpdated"
    
    echo "成功: $group_id:$artifact_id:$version"
    return 0
}

# 递归解析并下载父 POM
download_parent_poms() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return
    fi
    
    # 提取 parent 信息
    local parent_group=$(grep -A 5 "<parent>" "$pom_file" 2>/dev/null | grep "<groupId>" | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | xargs)
    local parent_artifact=$(grep -A 5 "<parent>" "$pom_file" 2>/dev/null | grep "<artifactId>" | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | xargs)
    local parent_version=$(grep -A 5 "<parent>" "$pom_file" 2>/dev/null | grep "<version>" | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | xargs)
    
    if [ -n "$parent_group" ] && [ -n "$parent_artifact" ] && [ -n "$parent_version" ]; then
        echo "发现父 POM: $parent_group:$parent_artifact:$parent_version"
        
        if download_pom "$parent_group" "$parent_artifact" "$parent_version"; then
            local group_path=$(echo $parent_group | tr '.' '/')
            local parent_pom="$LOCAL_REPO/$group_path/$parent_artifact/$parent_version/$parent_artifact-$parent_version.pom"
            download_parent_poms "$parent_pom"
        fi
    fi
}

# 从项目 POM 开始
echo "=== 开始下载所有父 POM ==="
download_parent_poms "/workspace/credit-platform/pom.xml"

echo ""
echo "=== 下载完成 ==="
