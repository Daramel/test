#!/bin/bash
export MAVEN_HOME=/opt/apache-maven-3.9.6
export PATH=$MAVEN_HOME/bin:$PATH
cd /workspace/credit-platform

BASE_URL="https://maven.aliyun.com/repository/public"
REPO_DIR="/root/.m2/repository"
MAX_RETRIES=50
RETRY=0

download_artifact() {
  local artifact="$1"
  
  IFS=':' read -ra parts <<< "$artifact"
  local numParts=${#parts[@]}
  
  if [ $numParts -lt 4 ]; then
    return 1
  fi
  
  local groupId="${parts[0]}"
  local artifactId="${parts[1]}"
  local version="${parts[$((numParts-1))]}"
  local packaging="${parts[2]}"
  local classifier=""
  
  if [ $numParts -eq 5 ]; then
    classifier="${parts[3]}"
  fi
  
  local groupPath=$(echo "$groupId" | tr '.' '/')
  local fileName="${artifactId}-${version}"
  if [ -n "$classifier" ]; then
    fileName="${fileName}-${classifier}"
  fi
  fileName="${fileName}.${packaging}"
  
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
      echo "  已下载: $relPath"
      return 0
    fi
    sleep 1
  done
  
  rm -f "$localPath"
  return 1
}

while [ $RETRY -lt $MAX_RETRIES ]; do
  RETRY=$((RETRY + 1))
  echo ""
  echo "=========================================="
  echo "=== 第 $RETRY 次尝试编译 ==="
  echo "=========================================="
  
  mvn clean package -DskipTests 2>&1 | tee /tmp/mvn_build_$RETRY.log
  MVN_EXIT=${PIPESTATUS[0]}
  
  if [ $MVN_EXIT -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "=== 编译成功！ ==="
    echo "=========================================="
    exit 0
  fi
  
  echo ""
  echo "=== 编译失败，正在收集缺失的依赖 ==="
  
  ARTIFACTS=$(grep -oP 'Could not transfer artifact [^ ]+' /tmp/mvn_build_$RETRY.log | \
    sed 's/Could not transfer artifact //' | sort -u)
  
  # 也从插件解析错误中提取
  PLUGINS=$(grep -oP 'Plugin [^ ]+ or one of its dependencies' /tmp/mvn_build_$RETRY.log | \
    sed 's/Plugin //' | sed 's/ or one of its dependencies//' | sort -u)
  
  for plugin in $PLUGINS; do
    IFS=':' read -ra p <<< "$plugin"
    if [ ${#p[@]} -ge 3 ]; then
      ARTIFACTS="$ARTIFACTS
${p[0]}:${p[1]}:pom:${p[2]}"
    fi
  done
  
  TOTAL=$(echo "$ARTIFACTS" | grep -v '^$' | wc -l)
  echo "发现 $TOTAL 个缺失的依赖"
  
  if [ $TOTAL -eq 0 ]; then
    echo "无法识别缺失的依赖，退出..."
    exit 1
  fi
  
  COUNT=0
  echo "$ARTIFACTS" | grep -v '^$' | while read artifact; do
    COUNT=$((COUNT + 1))
    echo "[$COUNT/$TOTAL] $artifact"
    download_artifact "$artifact"
  done
  
  find "$REPO_DIR" -name "*.lastUpdated" -delete 2>/dev/null
  find "$REPO_DIR" -name "_remote.repositories" -delete 2>/dev/null
  
  echo ""
  echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
  echo "等待 3 秒后重试..."
  sleep 3
done

echo ""
echo "=== 达到最大重试次数 ($MAX_RETRIES)，编译失败 ==="
exit 1
