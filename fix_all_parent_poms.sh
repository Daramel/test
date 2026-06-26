#!/bin/bash

REPO_URL="https://maven.aliyun.com/repository/public"
LOCAL_REPO="/root/.m2/repository"

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

download_parent_poms() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return 1
    fi
    
    local changed=0
    
    # 提取 parent
    local parent_group=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<groupId>" | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | xargs)
    local parent_artifact=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<artifactId>" | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | xargs)
    local parent_version=$(grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep "<version>" | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | xargs)
    
    if [ -n "$parent_group" ] && [ -n "$parent_artifact" ] && [ -n "$parent_version" ] && ! [[ "$parent_version" =~ \$\{ ]]; then
        local group_path=$(echo $parent_group | tr '.' '/')
        local parent_pom="$LOCAL_REPO/$group_path/$parent_artifact/$parent_version/$parent_artifact-$parent_version.pom"
        
        if [ ! -f "$parent_pom" ] || [ ! -s "$parent_pom" ]; then
            if download_pom "$parent_group" "$parent_artifact" "$parent_version"; then
                changed=1
                if download_parent_poms "$parent_pom"; then
                    changed=1
                fi
            fi
        fi
    fi
    
    return $changed
}

echo "=== 修复所有 POM 的父 POM ==="

total_changed=1
round=0

while [ $total_changed -gt 0 ] && [ $round -lt 10 ]; do
    round=$((round + 1))
    total_changed=0
    
    echo ""
    echo "--- 第 $round 轮 ---"
    
    while IFS= read -r pom_file; do
        if download_parent_poms "$pom_file"; then
            total_changed=$((total_changed + 1))
        fi
    done < <(find "$LOCAL_REPO" -name "*.pom" -type f)
    
    echo "本轮修复了 $total_changed 个 POM 的父 POM"
done

echo ""
echo "=== 完成 ==="
echo "总共 $round 轮后完成，共 $(find "$LOCAL_REPO" -name "*.pom" -type f | wc -l) 个 POM 文件"
