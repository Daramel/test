#!/bin/bash
# 智能下载脚本：递归扫描POM并下载所有依赖

REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
PROCESSED="/tmp/smart_download_processed.txt"
QUEUE="/tmp/smart_download_queue.txt"

mkdir -p "$REPO_DIR"
> "$PROCESSED"
> "$QUEUE"

download_file() {
  local relPath="$1"
  local localPath="$REPO_DIR/$relPath"
  local url="$BASE_URL/$relPath"
  
  if [ -f "$localPath" ] && [ -s "$localPath" ]; then
    return 0
  fi
  
  mkdir -p "$(dirname "$localPath")"
  
  for attempt in 1 2 3; do
    wget -q --timeout=30 -O "$localPath" "$url" 2>/dev/null
    if [ $? -eq 0 ] && [ -s "$localPath" ]; then
      return 0
    fi
    sleep 1
  done
  
  rm -f "$localPath"
  return 1
}

queue_artifact() {
  local groupId="$1"
  local artifactId="$2"
  local version="$3"
  
  # 跳过变量版本
  if [[ "$version" == *'${'* ]]; then
    return 1
  fi
  
  local groupPath=$(echo "$groupId" | tr '.' '/')
  local key="$groupId:$artifactId:$version"
  
  if grep -q "^$key$" "$PROCESSED" 2>/dev/null; then
    return 0
  fi
  
  echo "$key" >> "$PROCESSED"
  
  # 下载POM
  local pomPath="$groupPath/$artifactId/$version/$artifactId-$version.pom"
  if download_file "$pomPath"; then
    echo "POM: $groupId:$artifactId:$version"
    # 把新POM加入队列以便解析
    echo "$pomPath" >> "$QUEUE"
  fi
  
  # 下载JAR
  local jarPath="$groupPath/$artifactId/$version/$artifactId-$version.jar"
  download_file "$jarPath" && echo "JAR: $groupId:$artifactId:$version"
}

extract_deps_from_pom() {
  local pomFile="$1"
  
  if [ ! -f "$pomFile" ] || [ ! -s "$pomFile" ]; then
    return
  fi
  
  local inDependencies=0
  local inDependency=0
  local inDepMgmt=0
  local depGroup=""
  local depArtifact=""
  local depVersion=""
  
  while IFS= read -r line; do
    # 检测dependencyManagement
    if echo "$line" | grep -q '<dependencyManagement>'; then
      inDepMgmt=1
      continue
    fi
    if echo "$line" | grep -q '</dependencyManagement>'; then
      inDepMgmt=0
      continue
    fi
    
    # 只处理dependencies，不处理dependencyManagement（太多了）
    if [ $inDepMgmt -eq 1 ]; then
      continue
    fi
    
    if echo "$line" | grep -q '<dependencies>'; then
      inDependencies=1
      continue
    fi
    if echo "$line" | grep -q '</dependencies>'; then
      inDependencies=0
      continue
    fi
    
    if [ $inDependencies -eq 1 ]; then
      if echo "$line" | grep -q '<dependency>'; then
        inDependency=1
        depGroup=""
        depArtifact=""
        depVersion=""
        continue
      fi
      if echo "$line" | grep -q '</dependency>'; then
        inDependency=0
        if [ -n "$depGroup" ] && [ -n "$depArtifact" ] && [ -n "$depVersion" ]; then
          queue_artifact "$depGroup" "$depArtifact" "$depVersion"
        fi
        continue
      fi
      
      if [ $inDependency -eq 1 ]; then
        if echo "$line" | grep -q '<groupId>'; then
          depGroup=$(echo "$line" | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d ' ')
        elif echo "$line" | grep -q '<artifactId>'; then
          depArtifact=$(echo "$line" | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
        elif echo "$line" | grep -q '<version>'; then
          depVersion=$(echo "$line" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
        fi
      fi
    fi
  done < "$pomFile"
  
  # 也提取parent
  local parentGroup=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<groupId>' | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d ' ')
  local parentArtifact=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<artifactId>' | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
  local parentVersion=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<version>' | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
  
  if [ -n "$parentGroup" ] && [ -n "$parentArtifact" ] && [ -n "$parentVersion" ]; then
    queue_artifact "$parentGroup" "$parentArtifact" "$parentVersion"
  fi
}

echo "=== 智能下载依赖 ==="
echo "扫描现有POM文件..."

# 先找到所有已有的POM文件加入队列
find "$REPO_DIR" -name "*.pom" -type f | while read pom; do
  relPath="${pom#$REPO_DIR/}"
  echo "$relPath" >> "$QUEUE"
done

# 也把项目POM加进去
echo "添加项目POM..."
cp /workspace/credit-platform/pom.xml /tmp/project_pom.xml
echo "/tmp/project_pom.xml" >> "$QUEUE"

# 循环处理队列
echo "开始递归处理依赖..."
processed_count=0
while true; do
  # 读取队列中的第一个文件
  pomRel=$(head -1 "$QUEUE" 2>/dev/null)
  if [ -z "$pomRel" ]; then
    break
  fi
  
  # 从队列中移除
  tail -n +2 "$QUEUE" > "${QUEUE}.tmp" && mv "${QUEUE}.tmp" "$QUEUE"
  
  if [ -f "$pomRel" ]; then
    # 绝对路径（如项目POM）
    pomFile="$pomRel"
  else
    pomFile="$REPO_DIR/$pomRel"
  fi
  
  if [ ! -f "$pomFile" ]; then
    continue
  fi
  
  # 检查是否已处理过这个POM
  if grep -q "$pomRel" "$PROCESSED.pom" 2>/dev/null; then
    continue
  fi
  echo "$pomRel" >> "$PROCESSED.pom"
  
  processed_count=$((processed_count + 1))
  echo "[$processed_count] 解析: $pomRel"
  extract_deps_from_pom "$pomFile"
done

echo ""
echo "=== 下载完成 ==="
echo "已处理POM数: $processed_count"
echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
echo "仓库文件数: $(find "$REPO_DIR" -type f | wc -l)"
