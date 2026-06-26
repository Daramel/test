#!/bin/bash
# 递归下载所有POM文件及其父依赖

REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
DOWNLOADED="/tmp/downloaded_poms_recursive.txt"
mkdir -p "$REPO_DIR"
> "$DOWNLOADED"

download_pom() {
  local groupId="$1"
  local artifactId="$2"
  local version="$3"
  
  local groupPath=$(echo "$groupId" | tr '.' '/')
  local relPath="$groupPath/$artifactId/$version/$artifactId-$version.pom"
  local localPath="$REPO_DIR/$relPath"
  local url="$BASE_URL/$relPath"
  
  # 检查是否已处理过
  if grep -q "$relPath" "$DOWNLOADED" 2>/dev/null; then
    return 0
  fi
  
  # 如果本地没有，下载
  if [ ! -f "$localPath" ] || [ ! -s "$localPath" ]; then
    mkdir -p "$(dirname "$localPath")"
    for attempt in 1 2 3; do
      wget -q --timeout=30 -O "$localPath" "$url" 2>/dev/null
      if [ $? -eq 0 ] && [ -s "$localPath" ]; then
        echo "下载: $relPath"
        break
      fi
      sleep 1
    done
  fi
  
  echo "$relPath" >> "$DOWNLOADED"
  
  # 如果文件不存在，返回
  if [ ! -f "$localPath" ] || [ ! -s "$localPath" ]; then
    return 1
  fi
  
  # 解析父POM
  local parentGroup=$(grep -A10 '<parent>' "$localPath" 2>/dev/null | grep '<groupId>' | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d ' ')
  local parentArtifact=$(grep -A10 '<parent>' "$localPath" 2>/dev/null | grep '<artifactId>' | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
  local parentVersion=$(grep -A10 '<parent>' "$localPath" 2>/dev/null | grep '<version>' | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
  
  if [ -n "$parentGroup" ] && [ -n "$parentArtifact" ] && [ -n "$parentVersion" ]; then
    download_pom "$parentGroup" "$parentArtifact" "$parentVersion"
  fi
}

echo "=== 开始递归下载POM依赖 ==="

# 从项目POM开始
echo "解析项目POM..."
download_pom "com.credit" "credit-platform" "1.0.0"

# Spring Boot 相关
echo "下载Spring Boot POM..."
download_pom "org.springframework.boot" "spring-boot-starter-parent" "3.2.5"
download_pom "org.springframework.boot" "spring-boot-maven-plugin" "3.2.5"

# Maven 插件
echo "下载Maven插件POM..."
plugins=(
  "org.apache.maven.plugins:maven-clean-plugin:3.3.2"
  "org.apache.maven.plugins:maven-resources-plugin:3.3.1"
  "org.apache.maven.plugins:maven-compiler-plugin:3.11.0"
  "org.apache.maven.plugins:maven-surefire-plugin:3.1.2"
  "org.apache.maven.plugins:maven-jar-plugin:3.3.0"
  "org.apache.maven.plugins:maven-install-plugin:3.1.1"
  "org.apache.maven.plugins:maven-deploy-plugin:3.1.1"
  "org.apache.maven.plugins:maven-site-plugin:4.0.0-M13"
)

for plugin in "${plugins[@]}"; do
  IFS=':' read -ra parts <<< "$plugin"
  download_pom "${parts[0]}" "${parts[1]}" "${parts[2]}"
done

# 项目直接依赖的POM
echo "下载项目依赖POM..."
deps=(
  "org.springframework.boot:spring-boot-starter-web:3.2.5"
  "org.springframework.boot:spring-boot-starter-security:3.2.5"
  "org.springframework.boot:spring-boot-starter-data-redis:3.2.5"
  "org.springframework.boot:spring-boot-starter-validation:3.2.5"
  "org.springframework.boot:spring-boot-starter-test:3.2.5"
  "com.baomidou:mybatis-plus-boot-starter:3.5.6"
  "com.baomidou:mybatis-plus-core:3.5.6"
  "com.baomidou:mybatis-plus-extension:3.5.6"
  "com.mysql:mysql-connector-j:8.0.33"
  "io.jsonwebtoken:jjwt-api:0.12.5"
  "io.jsonwebtoken:jjwt-impl:0.12.5"
  "io.jsonwebtoken:jjwt-jackson:0.12.5"
  "cn.hutool:hutool-all:5.8.26"
  "org.projectlombok:lombok:1.18.30"
  "com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.5.0"
  "org.thymeleaf:thymeleaf:3.1.2.RELEASE"
  "org.thymeleaf:thymeleaf-spring6:3.1.2.RELEASE"
)

for dep in "${deps[@]}"; do
  IFS=':' read -ra parts <<< "$dep"
  download_pom "${parts[0]}" "${parts[1]}" "${parts[2]}"
done

echo ""
echo "=== POM下载完成 ==="
echo "已下载POM文件数: $(wc -l < "$DOWNLOADED")"
echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
