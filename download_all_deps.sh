#!/bin/bash
# 批量下载Maven依赖脚本
# 通过解析pom.xml，递归下载所有依赖的jar和pom

REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
DOWNLOADED="/tmp/downloaded_all.txt"
PROCESSED_POMS="/tmp/processed_poms.txt"

mkdir -p "$REPO_DIR"
> "$DOWNLOADED"
> "$PROCESSED_POMS"

download_file() {
  local relPath="$1"
  local localFile="$REPO_DIR/$relPath"
  local url="$BASE_URL/$relPath"
  
  if grep -q "$relPath" "$DOWNLOADED" 2>/dev/null; then
    return 0
  fi
  
  if [ -f "$localFile" ]; then
    echo "$relPath" >> "$DOWNLOADED"
    return 0
  fi
  
  local dir=$(dirname "$localFile")
  mkdir -p "$dir"
  
  wget -q --timeout=30 -O "$localFile" "$url" 2>/dev/null
  if [ $? -eq 0 ] && [ -s "$localFile" ]; then
    echo "下载: $relPath"
    echo "$relPath" >> "$DOWNLOADED"
    return 0
  else
    rm -f "$localFile"
    return 1
  fi
}

download_artifact() {
  local groupId="$1"
  local artifactId="$2"
  local version="$3"
  local packaging="${4:-jar}"
  
  local groupPath=$(echo "$groupId" | tr '.' '/')
  local basePath="$groupPath/$artifactId/$version"
  
  # 下载POM
  download_file "$basePath/$artifactId-$version.pom"
  
  # 下载JAR（如果不是pom类型）
  if [ "$packaging" != "pom" ]; then
    download_file "$basePath/$artifactId-$version.jar"
  fi
}

parse_pom_deps() {
  local pomFile="$1"
  
  if grep -q "$pomFile" "$PROCESSED_POMS" 2>/dev/null; then
    return 0
  fi
  echo "$pomFile" >> "$PROCESSED_POMS"
  
  if [ ! -f "$pomFile" ]; then
    return 1
  fi
  
  # 提取parent
  local parentGroup=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<groupId>' | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d ' ')
  local parentArtifact=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<artifactId>' | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
  local parentVersion=$(grep -A10 '<parent>' "$pomFile" 2>/dev/null | grep '<version>' | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
  
  if [ -n "$parentGroup" ] && [ -n "$parentArtifact" ] && [ -n "$parentVersion" ]; then
    local parentGroupPath=$(echo "$parentGroup" | tr '.' '/')
    local parentPomPath="$REPO_DIR/$parentGroupPath/$parentArtifact/$parentVersion/$parentArtifact-$parentVersion.pom"
    download_file "$parentGroupPath/$parentArtifact/$parentVersion/$parentArtifact-$parentVersion.pom"
    if [ -f "$parentPomPath" ]; then
      parse_pom_deps "$parentPomPath"
    fi
  fi
  
  # 提取dependenciesManagement中的依赖（用于BOM）
  local inDepMgmt=0
  local inDeps=0
  local depGroup=""
  local depArtifact=""
  local depVersion=""
  local depScope=""
  
  while IFS= read -r line; do
    if echo "$line" | grep -q '<dependencyManagement>'; then
      inDepMgmt=1
      continue
    fi
    if echo "$line" | grep -q '</dependencyManagement>'; then
      inDepMgmt=0
      continue
    fi
    if echo "$line" | grep -q '<dependencies>' && [ $inDepMgmt -eq 1 ]; then
      inDeps=1
      continue
    fi
    if echo "$line" | grep -q '</dependencies>' && [ $inDepMgmt -eq 1 ]; then
      inDeps=0
      continue
    fi
    
    if [ $inDeps -eq 1 ] && [ $inDepMgmt -eq 1 ]; then
      if echo "$line" | grep -q '<groupId>'; then
        depGroup=$(echo "$line" | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d ' ')
      elif echo "$line" | grep -q '<artifactId>'; then
        depArtifact=$(echo "$line" | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
      elif echo "$line" | grep -q '<version>'; then
        depVersion=$(echo "$line" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
      elif echo "$line" | grep -q '<scope>'; then
        depScope=$(echo "$line" | sed 's/.*<scope>\(.*\)<\/scope>.*/\1/' | tr -d ' ')
      elif echo "$line" | grep -q '</dependency>'; then
        if [ -n "$depGroup" ] && [ -n "$depArtifact" ] && [ -n "$depVersion" ] && [[ "$depVersion" != *'${'* ]]; then
          download_artifact "$depGroup" "$depArtifact" "$depVersion" "pom"
        fi
        depGroup=""
        depArtifact=""
        depVersion=""
        depScope=""
      fi
    fi
  done < "$pomFile"
}

echo "=== 开始下载依赖 ==="

# 首先下载Spring Boot父POM及其依赖管理
echo "下载Spring Boot相关POM..."
download_artifact "org.springframework.boot" "spring-boot-starter-parent" "3.2.5" "pom"
download_artifact "org.springframework.boot" "spring-boot-dependencies" "3.2.5" "pom"
download_artifact "org.springframework.boot" "spring-boot-parent" "3.2.5" "pom"

# 解析spring-boot-dependencies的POM以获取所有受管依赖版本
SPRING_BOOT_DEPS_POM="$REPO_DIR/org/springframework/boot/spring-boot-dependencies/3.2.5/spring-boot-dependencies-3.2.5.pom"
if [ -f "$SPRING_BOOT_DEPS_POM" ]; then
  echo "解析Spring Boot依赖管理POM..."
  parse_pom_deps "$SPRING_BOOT_DEPS_POM"
fi

# 下载插件
echo "下载Maven插件..."
download_artifact "org.springframework.boot" "spring-boot-maven-plugin" "3.2.5" "maven-plugin"

# 项目直接依赖列表
echo "下载项目直接依赖..."
deps=(
  "org.springframework.boot:spring-boot-starter-web:3.2.5"
  "org.springframework.boot:spring-boot-starter-security:3.2.5"
  "org.springframework.boot:spring-boot-starter-data-redis:3.2.5"
  "org.springframework.boot:spring-boot-starter-validation:3.2.5"
  "org.springframework.boot:spring-boot-starter-test:3.2.5"
  "com.baomidou:mybatis-plus-boot-starter:3.5.6"
  "com.mysql:mysql-connector-j:8.0.33"
  "io.jsonwebtoken:jjwt-api:0.12.5"
  "io.jsonwebtoken:jjwt-impl:0.12.5"
  "io.jsonwebtoken:jjwt-jackson:0.12.5"
  "cn.hutool:hutool-all:5.8.26"
  "org.projectlombok:lombok:1.18.30"
  "com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.5.0"
  "org.thymeleaf:thymeleaf:3.2.5"
  "org.thymeleaf:thymeleaf-spring6:3.2.5"
)

for dep in "${deps[@]}"; do
  IFS=':' read -ra parts <<< "$dep"
  download_artifact "${parts[0]}" "${parts[1]}" "${parts[2]}"
done

echo "=== 下载完成 ==="
echo "已下载文件数: $(wc -l < "$DOWNLOADED")"
echo "仓库大小: $(du -sh "$REPO_DIR" | cut -f1)"
