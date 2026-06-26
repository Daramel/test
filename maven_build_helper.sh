#!/bin/bash
export MAVEN_HOME=/opt/apache-maven-3.9.6
export PATH=$MAVEN_HOME/bin:$PATH
cd /workspace/credit-platform
BASE_URL="https://maven.aliyun.com/repository/public"
REPO_DIR="/root/.m2/repository"
MAX_RETRIES=30
RETRY=0

while [ $RETRY -lt $MAX_RETRIES ]; do
  RETRY=$((RETRY + 1))
  echo "=== 第 $RETRY 次尝试编译 ==="
  
  mvn clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=3 -Dmaven.wagon.httpconnectionManager.ttlSeconds=25 2>&1 | tee /tmp/mvn_retry_$RETRY.log
  
  if [ $? -eq 0 ]; then
    echo "=== 编译成功！ ==="
    exit 0
  fi
  
  echo "=== 编译失败，正在补全缺失的依赖 ==="
  
  grep -oP 'Could not transfer artifact [^ ]+' /tmp/mvn_retry_$RETRY.log | \
    sed 's/Could not transfer artifact //' | sort -u | while read artifact; do
    
    # 解析 artifact 坐标格式: groupId:artifactId:type:version
    # 或者 groupId:artifactId:packaging:classifier:version
    IFS=':' read -ra parts <<< "$artifact"
    
    groupId="${parts[0]}"
    artifactId="${parts[1]}"
    
    if [ ${#parts[@]} -eq 4 ]; then
      packaging="${parts[2]}"
      version="${parts[3]}"
      classifier=""
    elif [ ${#parts[@]} -eq 5 ]; then
      packaging="${parts[2]}"
      classifier="${parts[3]}"
      version="${parts[4]}"
    else
      continue
    fi
    
    groupPath=$(echo "$groupId" | tr '.' '/')
    
    if [ -n "$classifier" ]; then
      fileName="${artifactId}-${version}-${classifier}.${packaging}"
    else
      fileName="${artifactId}-${version}.${packaging}"
    fi
    
    relPath="$groupPath/$artifactId/$version/$fileName"
    local_path="$REPO_DIR/$relPath"
    url="$BASE_URL/$relPath"
    
    if [ ! -f "$local_path" ]; then
      dir=$(dirname "$local_path")
      mkdir -p "$dir"
      echo "下载: $relPath"
      wget -q --timeout=30 -O "$local_path" "$url" 2>/dev/null
      
      # 如果下载的是POM文件，也尝试下载对应的JAR
      if [ "$packaging" = "pom" ]; then
        jarRelPath="$groupPath/$artifactId/$version/$artifactId-$version.jar"
        jarLocalPath="$REPO_DIR/$jarRelPath"
        jarUrl="$BASE_URL/$jarRelPath"
        if [ ! -f "$jarLocalPath" ]; then
          wget -q --timeout=30 -O "$jarLocalPath" "$jarUrl" 2>/dev/null
        fi
      fi
    fi
  done
  
  # 也尝试解析缺失的插件POM
  grep -oP 'Plugin [^ ]+ or one of its dependencies could not be resolved' /tmp/mvn_retry_$RETRY.log | \
    sed 's/Plugin //' | sed 's/ or one of its dependencies could not be resolved//' | sort -u | while read plugin; do
    IFS=':' read -ra parts <<< "$plugin"
    if [ ${#parts[@]} -ge 3 ]; then
      groupId="${parts[0]}"
      artifactId="${parts[1]}"
      version="${parts[2]}"
      groupPath=$(echo "$groupId" | tr '.' '/')
      relPath="$groupPath/$artifactId/$version/$artifactId-$version.pom"
      local_path="$REPO_DIR/$relPath"
      url="$BASE_URL/$relPath"
      if [ ! -f "$local_path" ]; then
        dir=$(dirname "$local_path")
        mkdir -p "$dir"
        echo "下载插件POM: $relPath"
        wget -q --timeout=30 -O "$local_path" "$url" 2>/dev/null
      fi
    fi
  done
  
  find "$REPO_DIR" -name "*.lastUpdated" -delete
  echo "仓库大小: $(du -sh $REPO_DIR | cut -f1)"
  sleep 2
done

echo "=== 达到最大重试次数，编译失败 ==="
exit 1