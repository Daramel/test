#!/bin/bash
set -e

PROJECT_DIR="/workspace/credit-platform"
REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
LOG_FILE="/tmp/maven_build.log"
MAX_RETRIES=20

download_attempt=0

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
    echo "  下载成功: $groupId:$artifactId:$version:$type"
    return 0
  fi
  
  rm -f "$localPath"
  echo "  下载失败: $groupId:$artifactId:$version:$type"
  return 1
}

download_with_parent() {
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
      download_with_parent "$parentGroup" "$parentArtifact" "$parentVersion"
    fi
  fi
}

extract_missing_deps() {
  local log_file="$1"
  local deps=""
  
  while IFS= read -r line; do
    if echo "$line" | grep -q 'Could not resolve dependencies for project'; then
      continue
    fi
    
    if echo "$line" | grep -q 'Failed to collect dependencies at'; then
      dep=$(echo "$line" | sed 's/.*Failed to collect dependencies at //' | sed 's/:jar:/:/' | sed 's/:pom:/:/')
      echo "$dep"
    fi
    
    if echo "$line" | grep -q 'The following artifacts could not be resolved:'; then
      art=$(echo "$line" | sed 's/.*The following artifacts could not be resolved: //' | sed 's/ (absent).*//' | sed 's/:jar:/:/' | sed 's/:pom:/:/')
      echo "$art"
    fi
    
    if echo "$line" | grep -q 'Could not find artifact'; then
      art=$(echo "$line" | sed 's/.*Could not find artifact //' | sed 's/ in.*//' | sed 's/:jar:/:/' | sed 's/:pom:/:/')
      echo "$art"
    fi
    
  done < "$log_file"
}

export MAVEN_HOME=/opt/apache-maven-3.9.6
export PATH=$MAVEN_HOME/bin:$PATH

cd "$PROJECT_DIR"

echo "开始快速编译模式"
echo "仓库目录: $REPO_DIR"
echo ""

for ((i=1; i<=MAX_RETRIES; i++)); do
  echo "=========================================="
  echo "=== 第 $i 次尝试编译 ==="
  echo "=========================================="
  
  find "$REPO_DIR" -name "*.lastUpdated" -delete 2>/dev/null || true
  find "$REPO_DIR" -name "_remote.repositories" -delete 2>/dev/null || true
  
  mvn clean package -DskipTests -o > "$LOG_FILE" 2>&1 && {
    echo ""
    echo "=========================================="
    echo "=== 编译成功！ ==="
    echo "=========================================="
    tail -20 "$LOG_FILE"
    exit 0
  }
  
  echo "编译失败，提取缺失的依赖..."
  
  missing_deps=$(extract_missing_deps "$LOG_FILE")
  
  if [ -z "$missing_deps" ]; then
    echo "无法提取缺失的依赖，显示最后 30 行错误："
    tail -30 "$LOG_FILE"
    exit 1
  fi
  
  echo ""
  echo "发现缺失的依赖："
  echo "$missing_deps"
  echo ""
  echo "开始下载..."
  
  downloaded=0
  while IFS= read -r dep; do
    [ -z "$dep" ] && continue
    
    IFS=':' read -ra parts <<< "$dep"
    groupId="${parts[0]}"
    artifactId="${parts[1]}"
    version="${parts[2]}"
    type="${parts[3]:-jar}"
    
    if [ "$type" = "pom" ]; then
      download_with_parent "$groupId" "$artifactId" "$version" && downloaded=$((downloaded + 1))
    else
      download_with_parent "$groupId" "$artifactId" "$version"
      download_artifact "$groupId" "$artifactId" "$version" "jar" && downloaded=$((downloaded + 1))
    fi
    
  done <<< "$missing_deps"
  
  echo ""
  echo "本轮下载了 $downloaded 个依赖"
  echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
  echo ""
  
done

echo "达到最大重试次数，编译失败"
tail -50 "$LOG_FILE"
exit 1
