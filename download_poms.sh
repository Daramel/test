#!/bin/bash
# 递归下载POM父依赖
REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
DOWNLOADED="/tmp/downloaded_poms.txt"
> "$DOWNLOADED"

download_pom() {
  local groupPath=$1
  local artifact=$2
  local version=$3
  
  local relPath="$groupPath/$artifact/$version/$artifact-$version.pom"
  local localFile="$REPO_DIR/$relPath"
  
  # 检查是否已下载过
  if grep -q "$relPath" "$DOWNLOADED" 2>/dev/null; then
    return 0
  fi
  
  if [ ! -f "$localFile" ]; then
    local dir=$(dirname "$localFile")
    mkdir -p "$dir"
    wget -q --timeout=30 -O "$localFile" "$BASE_URL/$relPath" 2>/dev/null
    if [ $? -ne 0 ]; then
      return 1
    fi
    echo "下载: $artifact-$version.pom"
  fi
  
  echo "$relPath" >> "$DOWNLOADED"
  
  # 解析父POM
  local parentGroup=$(grep -A5 '<parent>' "$localFile" 2>/dev/null | grep '<groupId>' | head -1 | sed 's/.*<groupId>\(.*\)<\/groupId>.*/\1/' | tr -d ' ')
  local parentArtifact=$(grep -A5 '<parent>' "$localFile" 2>/dev/null | grep '<artifactId>' | head -1 | sed 's/.*<artifactId>\(.*\)<\/artifactId>.*/\1/' | tr -d ' ')
  local parentVersion=$(grep -A5 '<parent>' "$localFile" 2>/dev/null | grep '<version>' | head -1 | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')
  
  if [ -n "$parentGroup" ] && [ -n "$parentArtifact" ] && [ -n "$parentVersion" ]; then
    local parentGroupPath=$(echo "$parentGroup" | tr '.' '/')
    download_pom "$parentGroupPath" "$parentArtifact" "$parentVersion"
  fi
}

# 从项目POM开始
echo "开始递归下载POM依赖..."

# 下载Spring Boot相关的核心BOM
boms=(
  "org.springframework.boot:spring-boot-starter-parent:3.2.5"
  "org.springframework.boot:spring-boot-dependencies:3.2.5"
  "org.springframework.boot:spring-boot-parent:3.2.5"
  "org.springframework:spring-framework-bom:6.1.6"
  "com.fasterxml.jackson:jackson-bom:2.15.4"
  "io.projectreactor:reactor-bom:2023.0.5"
  "io.micrometer:micrometer-bom:1.12.5"
  "org.apache.logging.log4j:log4j-bom:2.21.1"
  "org.junit:junit-bom:5.10.2"
  "org.mockito:mockito-bom:5.7.0"
  "io.netty:netty-bom:4.1.109.Final"
  "org.springframework.security:spring-security-bom:6.2.4"
  "org.springframework.data:spring-data-bom:2023.1.5"
  "org.springframework.batch:spring-batch-bom:5.1.1"
  "org.springframework.integration:spring-integration-bom:6.2.4"
  "org.springframework.amqp:spring-amqp-bom:3.1.4"
  "org.springframework.ws:spring-ws-bom:4.0.10"
  "org.springframework.session:spring-session-bom:3.2.2"
  "org.springframework.pulsar:spring-pulsar-bom:1.0.5"
  "org.springframework.restdocs:spring-restdocs-bom:3.0.1"
  "org.infinispan:infinispan-bom:14.0.27.Final"
  "org.glassfish.jaxb:jaxb-bom:4.0.5"
  "org.glassfish.jersey:jersey-bom:3.1.6"
  "org.eclipse.jetty:jetty-bom:12.0.8"
  "org.eclipse.jetty.ee10:jetty-ee10-bom:12.0.8"
  "org.jetbrains.kotlin:kotlin-bom:1.9.23"
  "org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"
  "org.jetbrains.kotlinx:kotlinx-serialization-bom:1.6.3"
  "io.zipkin.brave:brave-bom:5.16.0"
  "io.zipkin.reporter2:zipkin-reporter-bom:2.16.3"
  "com.datastax.oss:java-driver-bom:4.17.0"
  "io.dropwizard.metrics:metrics-bom:4.2.25"
  "org.apache.groovy:groovy-bom:4.0.21"
  "io.micrometer:micrometer-tracing-bom:1.2.5"
  "com.squareup.okhttp3:okhttp-bom:4.12.0"
  "io.opentelemetry:opentelemetry-bom:1.31.0"
  "com.oracle.database.jdbc:ojdbc-bom:21.9.0.0"
  "io.prometheus:simpleclient_bom:0.16.0"
  "com.querydsl:querydsl-bom:5.0.0"
  "io.rest-assured:rest-assured-bom:5.3.2"
  "io.rsocket:rsocket-bom:1.1.3"
  "org.seleniumhq.selenium:selenium-bom:4.14.1"
  "org.testcontainers:testcontainers-bom:1.19.7"
  "org.assertj:assertj-bom:3.24.2"
)

for bom in "${boms[@]}"; do
  IFS=':' read -ra parts <<< "$bom"
  groupPath=$(echo "${parts[0]}" | tr '.' '/')
  download_pom "$groupPath" "${parts[1]}" "${parts[2]}"
done

echo "=== POM下载完成 ==="
echo "已下载 $(wc -l < "$DOWNLOADED") 个POM文件"
du -sh "$REPO_DIR"