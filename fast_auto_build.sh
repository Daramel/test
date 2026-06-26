#!/bin/bash

PROJECT_DIR="/workspace/credit-platform"
REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
MAX_RETRIES=100

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
  
  for attempt in 1 2 3; do
    wget -q --timeout=30 -O "$localPath" "$url" 2>/dev/null
    if [ $? -eq 0 ] && [ -s "$localPath" ]; then
      echo "  下载: $groupId:$artifactId:$version:$type"
      return 0
    fi
    sleep 1
  done
  
  rm -f "$localPath"
  echo "  下载失败: $groupId:$artifactId:$version:$type"
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

extract_first_missing() {
  local log="$1"
  
  # 从 "The following artifacts could not be resolved:" 提取
  local missing=$(grep "The following artifacts could not be resolved:" "$log" | head -1 | sed 's/.*The following artifacts could not be resolved: //' | sed 's/ (absent).*//')
  
  if [ -n "$missing" ]; then
    echo "$missing"
    return 0
  fi
  
  # 从 "Could not find artifact" 提取
  missing=$(grep "Could not find artifact" "$log" | head -1 | sed 's/.*Could not find artifact //' | sed 's/ in.*//')
  
  if [ -n "$missing" ]; then
    echo "$missing"
    return 0
  fi
  
  # 从 "Failed to collect dependencies at" 提取
  missing=$(grep "Failed to collect dependencies at" "$log" | head -1 | sed 's/.*Failed to collect dependencies at //' | sed 's/ ->.*//')
  
  if [ -n "$missing" ]; then
    echo "$missing"
    return 0
  fi
  
  return 1
}

parse_artifact() {
  local art="$1"
  
  # 格式可能是 groupId:artifactId:type:version 或 groupId:artifactId:version
  local groupId=$(echo "$art" | cut -d: -f1)
  local artifactId=$(echo "$art" | cut -d: -f2)
  local part3=$(echo "$art" | cut -d: -f3)
  local part4=$(echo "$art" | cut -d: -f4)
  
  if [ "$part3" = "jar" ] || [ "$part3" = "pom" ]; then
    echo "$groupId $artifactId $part4 $part3"
  else
    echo "$groupId $artifactId $part3 jar"
  fi
}

echo "=========================================="
echo "快速自动编译脚本"
echo "=========================================="
echo ""

for ((i=1; i<=MAX_RETRIES; i++)); do
  echo "--- 第 $i 次尝试 ---"
  echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
  
  find "$REPO_DIR" -name "*.lastUpdated" -delete 2>/dev/null
  find "$REPO_DIR" -name "_remote.repositories" -delete 2>/dev/null
  
  mvn clean package -DskipTests -o > /tmp/build.log 2>&1
  
  if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "BUILD SUCCESS! 编译成功！"
    echo "=========================================="
    tail -15 /tmp/build.log
    exit 0
  fi
  
  # 提取第一个缺失的依赖
  missing=$(extract_first_missing /tmp/build.log)
  
  if [ -z "$missing" ]; then
    echo "无法解析错误，显示最后 30 行:"
    tail -30 /tmp/build.log
    exit 1
  fi
  
  echo "缺失依赖: $missing"
  echo "正在下载..."
  
  # 解析依赖坐标
  read groupId artifactId version type < <(parse_artifact "$missing")
  
  if [ -z "$groupId" ] || [ -z "$artifactId" ] || [ -z "$version" ]; then
    echo "无法解析依赖坐标: $missing"
    exit 1
  fi
  
  # 下载 POM 及其父 POM 链
  download_with_parents "$groupId" "$artifactId" "$version"
  
  # 如果是 JAR 类型，也下载 JAR
  if [ "$type" = "jar" ]; then
    download_artifact "$groupId" "$artifactId" "$version" "jar"
  fi
  
  echo ""
  sleep 1
done

echo "达到最大重试次数"
tail -50 /tmp/build.log
exit 1
