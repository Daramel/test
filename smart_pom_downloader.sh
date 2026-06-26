#!/bin/bash

REPO_URL="https://maven.aliyun.com/repository/public"
LOCAL_REPO="/root/.m2/repository"

download_pom() {
    local group_id=$1
    local artifact_id=$2
    local version=$3
    
    local group_path=$(echo $group_id | tr '.' '/')
    local pom_dir="$LOCAL_REPO/$group_path/$artifact_id/$version"
    local pom_file="$pom_dir/$artifact_id-$version.pom"
    
    if [ -f "$pom_file" ] && [ -s "$pom_file" ]; then
        return 0
    fi
    
    mkdir -p "$pom_dir"
    
    local url="$REPO_URL/$group_path/$artifact_id/$version/$artifact_id-$version.pom"
    
    if wget -q --timeout=30 --tries=3 -O "$pom_file" "$url" 2>/dev/null; then
        rm -f "$pom_file.lastUpdated"
        echo "下载: $group_id:$artifact_id:$version"
        return 0
    else
        rm -f "$pom_file"
        echo "失败: $group_id:$artifact_id:$version"
        return 1
    fi
}

extract_parent_from_pom() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return
    fi
    
    grep -A 10 "<parent>" "$pom_file" 2>/dev/null | grep -E "<groupId>|<artifactId>|<version>" | head -3
}

extract_import_poms_from_pom() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return
    fi
    
    grep -B 5 -A 10 "<scope>import</scope>" "$pom_file" 2>/dev/null | grep -E "<groupId>|<artifactId>|<version>"
}

process_pom_parents() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return 1
    fi
    
    local info=$(extract_parent_from_pom "$pom_file")
    if [ -z "$info" ]; then
        return 0
    fi
    
    local parent_group=$(echo "$info" | grep "<groupId>" | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | xargs)
    local parent_artifact=$(echo "$info" | grep "<artifactId>" | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | xargs)
    local parent_version=$(echo "$info" | grep "<version>" | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | xargs)
    
    if [ -z "$parent_group" ] || [ -z "$parent_artifact" ] || [ -z "$parent_version" ]; then
        return 0
    fi
    
    local group_path=$(echo $parent_group | tr '.' '/')
    local parent_pom="$LOCAL_REPO/$group_path/$parent_artifact/$parent_version/$parent_artifact-$parent_version.pom"
    
    if [ ! -f "$parent_pom" ] || [ ! -s "$parent_pom" ]; then
        download_pom "$parent_group" "$parent_artifact" "$parent_version"
        return 1
    fi
    
    return 0
}

process_import_poms() {
    local pom_file=$1
    
    if [ ! -f "$pom_file" ]; then
        return 1
    fi
    
    local changed=0
    
    while IFS= read -r line; do
        local group_id=$(echo "$line" | grep "<groupId>" | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | xargs)
        local artifact_id=$(echo "$line" | grep "<artifactId>" | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | xargs)
        local version=$(echo "$line" | grep "<version>" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | xargs)
        
        if [ -n "$group_id" ] && [ -n "$artifact_id" ] && [ -n "$version" ]; then
            local group_path=$(echo $group_id | tr '.' '/')
            local import_pom="$LOCAL_REPO/$group_path/$artifact_id/$version/$artifact_id-$version.pom"
            
            if [ ! -f "$import_pom" ] || [ ! -s "$import_pom" ]; then
                download_pom "$group_id" "$artifact_id" "$version"
                changed=1
            fi
        fi
    done < <(grep -B 5 -A 10 "<scope>import</scope>" "$pom_file" 2>/dev/null | grep -E "<groupId>|<artifactId>|<version>" | paste - - - )
    
    return $changed
}

echo "=== 智能 POM 下载器 ==="

for round in 1 2 3 4 5; do
    echo ""
    echo "--- 第 $round 轮 ---"
    
    changed=0
    
    while IFS= read -r pom_file; do
        if process_pom_parents "$pom_file"; then
            :
        else
            changed=1
        fi
        
        if process_import_poms "$pom_file"; then
            :
        else
            changed=1
        fi
    done < <(find "$LOCAL_REPO" -name "*.pom" -type f)
    
    if [ $changed -eq 0 ]; then
        echo "没有新的 POM 需要下载，完成！"
        break
    fi
done

echo ""
echo "=== 完成 ==="
echo "本地 POM 文件数量: $(find "$LOCAL_REPO" -name "*.pom" -type f | wc -l)"
