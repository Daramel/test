#!/bin/bash
# 从Maven输出中提取缺失的依赖并下载

REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
mkdir -p "$REPO_DIR"

download() {
  local groupId="$1"
  local artifactId="$2"
  local version="$3"
  local type="${4:-pom}"
  
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
      echo "OK: $groupId:$artifactId:$version:$type"
      return 0
    fi
    sleep 1
  done
  
  rm -f "$localPath"
  echo "FAIL: $groupId:$artifactId:$version:$type"
  return 1
}

echo "=== 下载缺失的依赖 ==="

# 从之前离线编译的警告中提取的缺失POM
missing_poms=(
  "io.micrometer:micrometer-commons:1.12.5"
  "ch.qos.logback:logback-core:1.4.14"
  "org.slf4j:slf4j-api:2.0.13"
  "com.fasterxml.jackson.core:jackson-core:2.15.4"
  "com.fasterxml.jackson.core:jackson-annotations:2.15.4"
  "org.apache.tomcat.embed:tomcat-embed-core:10.1.20"
  "org.apache.tomcat.embed:tomcat-embed-websocket:10.1.20"
  "org.springframework.security:spring-security-crypto:6.2.4"
  "io.netty:netty-common:4.1.109.Final"
  "io.netty:netty-handler:4.1.109.Final"
  "io.netty:netty-transport:4.1.109.Final"
  "io.projectreactor:reactor-core:3.6.5"
  "org.projectlombok:lombok:1.18.32"
  "com.github.xiaoymin:knife4j-core:4.5.0"
  "com.github.xiaoymin:knife4j-openapi3-ui:4.5.0"
  "org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0"
  "org.ow2.asm:asm:9.6"
  "jakarta.activation:jakarta.activation-api:2.1.3"
  "org.opentest4j:opentest4j:1.3.0"
  "org.junit.platform:junit-platform-commons:1.10.2"
  "org.junit.platform:junit-platform-engine:1.10.2"
  "net.bytebuddy:byte-buddy:1.14.13"
  "net.bytebuddy:byte-buddy-agent:1.14.13"
  "org.xmlunit:xmlunit-core:2.9.1"
  "org.ow2.asm:asm-bom:9.5"
  "org.codehaus.groovy:groovy-bom:3.0.19"
  "com.fasterxml.jackson:jackson-bom:2.15.2"
  "jakarta.platform:jakarta.jakartaee-bom:9.1.0"
  "org.eclipse.jetty:jetty-bom:9.4.53.v20231009"
  "org.junit:junit-bom:5.10.0"
  "io.fabric8:kubernetes-client-bom:5.12.4"
  "org.mockito:mockito-bom:4.11.0"
  "io.netty:netty-bom:4.1.97.Final"
  "org.springframework:spring-framework-bom:5.3.29"
  "org.slf4j:slf4j-bom:2.0.13"
  "org.junit:junit-bom:5.9.2"
)

echo "下载 POM 文件..."
for dep in "${missing_poms[@]}"; do
  IFS=':' read -ra parts <<< "$dep"
  if [ ${#parts[@]} -ge 3 ]; then
    download "${parts[0]}" "${parts[1]}" "${parts[2]}" "pom"
  fi
done

echo ""
echo "也下载对应的 JAR 文件..."
# 下载一些核心的JAR
core_jars=(
  "io.micrometer:micrometer-commons:1.12.5"
  "ch.qos.logback:logback-core:1.4.14"
  "org.slf4j:slf4j-api:2.0.13"
  "com.fasterxml.jackson.core:jackson-core:2.15.4"
  "com.fasterxml.jackson.core:jackson-annotations:2.15.4"
  "org.apache.tomcat.embed:tomcat-embed-core:10.1.20"
  "org.apache.tomcat.embed:tomcat-embed-websocket:10.1.20"
  "org.springframework.security:spring-security-crypto:6.2.4"
  "io.netty:netty-common:4.1.109.Final"
  "io.netty:netty-handler:4.1.109.Final"
  "io.netty:netty-transport:4.1.109.Final"
  "io.projectreactor:reactor-core:3.6.5"
  "org.projectlombok:lombok:1.18.32"
  "com.github.xiaoymin:knife4j-core:4.5.0"
  "org.ow2.asm:asm:9.6"
  "jakarta.activation:jakarta.activation-api:2.1.3"
  "org.opentest4j:opentest4j:1.3.0"
  "org.junit.platform:junit-platform-commons:1.10.2"
  "org.junit.platform:junit-platform-engine:1.10.2"
  "net.bytebuddy:byte-buddy:1.14.13"
  "net.bytebuddy:byte-buddy-agent:1.14.13"
  "org.xmlunit:xmlunit-core:2.9.1"
)

for dep in "${core_jars[@]}"; do
  IFS=':' read -ra parts <<< "$dep"
  if [ ${#parts[@]} -ge 3 ]; then
    download "${parts[0]}" "${parts[1]}" "${parts[2]}" "jar"
  fi
done

echo ""
echo "=== 下载完成 ==="
echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
