#!/bin/bash

PROJECT_DIR="/workspace/credit-platform"
REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
MAX_RETRIES=30

export MAVEN_HOME=/opt/apache-maven-3.9.6
export PATH=$MAVEN_HOME/bin:$PATH

cd "$PROJECT_DIR"

download_artifact() {
  local groupId="$1"
  local artifactId="$2"
  local version="$3"
  local type="${4:-jar}"
  
  local groupPath=$(echo "$groupId" | tr '.' '/')
  local fileName="${artifactId}-${version}.${type}"
  local relPath="$groupPath/$artifactId/$version/$fileName"
  local localPath="$REPO_DIR/$relPath"
  local url="$BASE_URL/$relPath"
  
  if [ -f "$localPath" ] && [ -s "$localPath" ]; then
    return 0
  fi
  
  mkdir -p "$(dirname "$localPath")"
  
  wget -q --timeout=30 -O "$localPath" "$url" 2>/dev/null
  if [ $? -eq 0 ] && [ -s "$localPath" ]; then
    echo "  下载: $groupId:$artifactId:$version:$type"
    return 0
  fi
  
  rm -f "$localPath"
  return 1
}

download_with_parents() {
  local groupId="$1"
  local artifactId="$2"
  local version="$3"
  
  download_artifact "$groupId" "$artifactId" "$version" "pom"
  
  local groupPath=$(echo "$groupId" | tr '.' '/')
  local pomFile="$REPO_DIR/$groupPath/$artifactId/$version/$artifactId-$version.pom"
  
  if [ -f "$pomFile" ]; then
    local parentGroup=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<groupId>' | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d ' ')
    local parentArtifact=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<artifactId>' | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
    local parentVersion=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<version>' | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
    
    if [ -n "$parentGroup" ] && [ -n "$parentArtifact" ] && [ -n "$parentVersion" ]; then
      download_with_parents "$parentGroup" "$parentArtifact" "$parentVersion"
    fi
  fi
}

extract_missing() {
  local log="$1"
  grep "The following artifacts could not be resolved:" "$log" | sed 's/.*The following artifacts could not be resolved: //' | sed 's/ (absent).*//'
}

echo "=========================================="
echo "自动编译脚本 - 自动补全缺失依赖"
echo "=========================================="
echo ""

for ((i=1; i<=MAX_RETRIES; i++)); do
  echo "--- 第 $i 次尝试 ---"
  echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
  
  find "$REPO_DIR" -name "*.lastUpdated" -delete 2>/dev/null
  find "$REPO_DIR" -name "_remote.repositories" -delete 2>/dev/null
  
  mvn clean package -DskipTests > /tmp/build.log 2>&1
  
  if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "BUILD SUCCESS! 编译成功！"
    echo "=========================================="
    tail -10 /tmp/build.log
    exit 0
  fi
  
  echo "编译失败，尝试用 wget 补全缺失依赖..."
  
  # 提取缺失的依赖
  missing=$(extract_missing /tmp/build.log)
  
  if [ -n "$missing" ]; then
    echo "发现缺失的依赖:"
    echo "$missing"
    echo ""
    
    while IFS= read -r art; do
      [ -z "$art" ] && continue
      
      # 解析坐标
      groupId=$(echo "$art" | cut -d: -f1)
      artifactId=$(echo "$art" | cut -d: -f2)
      type=$(echo "$art" | cut -d: -f3)
      version=$(echo "$art" | cut -d: -f4)
      
      if [ "$type" = "pom" ]; then
        download_with_parents "$groupId" "$artifactId" "$version"
      else
        download_with_parents "$groupId" "$artifactId" "$version"
        download_artifact "$groupId" "$artifactId" "$version" "jar"
      fi
    done <<< "$missing"
  fi
  
  # 也尝试从 "Could not find artifact" 提取
  grep "Could not find artifact" /tmp/build.log | sed 's/.*Could not find artifact //' | sed 's/ in.*//' | while read -r art; do
    [ -z "$art" ] && continue
    groupId=$(echo "$art" | cut -d: -f1)
    artifactId=$(echo "$art" | cut -d: -f2)
    version=$(echo "$art" | cut -d: -f4)
    download_with_parents "$groupId" "$artifactId" "$version"
    download_artifact "$groupId" "$artifactId" "$version" "jar"
  done
  
  echo ""
  sleep 2
done

echo "达到最大重试次数"
tail -50 /tmp/build.log
exit 1
