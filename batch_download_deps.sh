#!/bin/bash
# 批量下载Maven核心依赖

REPO_DIR="/root/.m2/repository"
BASE_URL="https://maven.aliyun.com/repository/public"
mkdir -p "$REPO_DIR"

download() {
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
      echo "OK: $relPath"
      return 0
    fi
    sleep 1
  done
  
  rm -f "$localPath"
  echo "FAIL: $relPath"
  return 1
}

echo "=== 开始批量下载依赖 ==="

# 1. Maven 核心插件
echo "--- Maven 插件 ---"
plugins=(
  "org/apache/maven/plugins/maven-clean-plugin/3.3.2/maven-clean-plugin-3.3.2.pom"
  "org/apache/maven/plugins/maven-clean-plugin/3.3.2/maven-clean-plugin-3.3.2.jar"
  "org/apache/maven/plugins/maven-resources-plugin/3.3.1/maven-resources-plugin-3.3.1.pom"
  "org/apache/maven/plugins/maven-resources-plugin/3.3.1/maven-resources-plugin-3.3.1.jar"
  "org/apache/maven/plugins/maven-compiler-plugin/3.11.0/maven-compiler-plugin-3.11.0.pom"
  "org/apache/maven/plugins/maven-compiler-plugin/3.11.0/maven-compiler-plugin-3.11.0.jar"
  "org/apache/maven/plugins/maven-surefire-plugin/3.1.2/maven-surefire-plugin-3.1.2.pom"
  "org/apache/maven/plugins/maven-surefire-plugin/3.1.2/maven-surefire-plugin-3.1.2.jar"
  "org/apache/maven/plugins/maven-jar-plugin/3.3.0/maven-jar-plugin-3.3.0.pom"
  "org/apache/maven/plugins/maven-jar-plugin/3.3.0/maven-jar-plugin-3.3.0.jar"
  "org/apache/maven/plugins/maven-install-plugin/3.1.1/maven-install-plugin-3.1.1.pom"
  "org/apache/maven/plugins/maven-install-plugin/3.1.1/maven-install-plugin-3.1.1.jar"
  "org/apache/maven/plugins/maven-deploy-plugin/3.1.1/maven-deploy-plugin-3.1.1.pom"
  "org/apache/maven/plugins/maven-deploy-plugin/3.1.1/maven-deploy-plugin-3.1.1.jar"
  "org/apache/maven/plugins/maven-site-plugin/4.0.0-M13/maven-site-plugin-4.0.0-M13.pom"
  "org/apache/maven/plugins/maven-site-plugin/4.0.0-M13/maven-site-plugin-4.0.0-M13.jar"
)

for p in "${plugins[@]}"; do
  download "$p"
done

# 2. Maven 插件父POM
echo "--- Maven 插件父POM ---"
pluginParents=(
  "org/apache/maven/plugins/maven-plugins/40/maven-plugins-40.pom"
  "org/apache/maven/maven-parent/40/maven-parent-40.pom"
  "org/apache/apache/30/apache-30.pom"
  "org/apache/maven/maven-core/3.9.6/maven-core-3.9.6.pom"
  "org/apache/maven/maven-model/3.9.6/maven-model-3.9.6.pom"
  "org/apache/maven/maven-settings/3.9.6/maven-settings-3.9.6.pom"
)

for p in "${pluginParents[@]}"; do
  download "$p"
done

# 3. Spring Boot 父POM
echo "--- Spring Boot 父POM ---"
springBootParents=(
  "org/springframework/boot/spring-boot-starter-parent/3.2.5/spring-boot-starter-parent-3.2.5.pom"
  "org/springframework/boot/spring-boot-dependencies/3.2.5/spring-boot-dependencies-3.2.5.pom"
  "org/springframework/boot/spring-boot-parent/3.2.5/spring-boot-parent-3.2.5.pom"
  "org/springframework/boot/spring-boot-maven-plugin/3.2.5/spring-boot-maven-plugin-3.2.5.pom"
  "org/springframework/boot/spring-boot-maven-plugin/3.2.5/spring-boot-maven-plugin-3.2.5.jar"
)

for p in "${springBootParents[@]}"; do
  download "$p"
done

# 4. Spring Boot 主要 starters
echo "--- Spring Boot Starters ---"
starters=(
  "spring-boot-starter-web"
  "spring-boot-starter-security"
  "spring-boot-starter-data-redis"
  "spring-boot-starter-validation"
  "spring-boot-starter-test"
  "spring-boot-starter"
  "spring-boot-starter-json"
  "spring-boot-starter-tomcat"
  "spring-boot-starter-logging"
)

for s in "${starters[@]}"; do
  download "org/springframework/boot/$s/3.2.5/$s-3.2.5.pom"
  download "org/springframework/boot/$s/3.2.5/$s-3.2.5.jar"
done

# 5. 项目直接依赖
echo "--- 项目直接依赖 ---"
directDeps=(
  "com/baomidou/mybatis-plus-boot-starter/3.5.6/mybatis-plus-boot-starter-3.5.6.pom"
  "com/baomidou/mybatis-plus-boot-starter/3.5.6/mybatis-plus-boot-starter-3.5.6.jar"
  "com/baomidou/mybatis-plus-core/3.5.6/mybatis-plus-core-3.5.6.pom"
  "com/baomidou/mybatis-plus-core/3.5.6/mybatis-plus-core-3.5.6.jar"
  "com/baomidou/mybatis-plus-extension/3.5.6/mybatis-plus-extension-3.5.6.pom"
  "com/baomidou/mybatis-plus-extension/3.5.6/mybatis-plus-extension-3.5.6.jar"
  "com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.pom"
  "com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
  "io/jsonwebtoken/jjwt-api/0.12.5/jjwt-api-0.12.5.pom"
  "io/jsonwebtoken/jjwt-api/0.12.5/jjwt-api-0.12.5.jar"
  "io/jsonwebtoken/jjwt-impl/0.12.5/jjwt-impl-0.12.5.pom"
  "io/jsonwebtoken/jjwt-impl/0.12.5/jjwt-impl-0.12.5.jar"
  "io/jsonwebtoken/jjwt-jackson/0.12.5/jjwt-jackson-0.12.5.pom"
  "io/jsonwebtoken/jjwt-jackson/0.12.5/jjwt-jackson-0.12.5.jar"
  "cn/hutool/hutool-all/5.8.26/hutool-all-5.8.26.pom"
  "cn/hutool/hutool-all/5.8.26/hutool-all-5.8.26.jar"
  "org/projectlombok/lombok/1.18.30/lombok-1.18.30.pom"
  "org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar"
  "com/github/xiaoymin/knife4j-openapi3-jakarta-spring-boot-starter/4.5.0/knife4j-openapi3-jakarta-spring-boot-starter-4.5.0.pom"
  "com/github/xiaoymin/knife4j-openapi3-jakarta-spring-boot-starter/4.5.0/knife4j-openapi3-jakarta-spring-boot-starter-4.5.0.jar"
  "org/thymeleaf/thymeleaf/3.2.5/thymeleaf-3.2.5.pom"
  "org/thymeleaf/thymeleaf/3.2.5/thymeleaf-3.2.5.jar"
  "org/thymeleaf/thymeleaf-spring6/3.2.5/thymeleaf-spring6-3.2.5.pom"
  "org/thymeleaf/thymeleaf-spring6/3.2.5/thymeleaf-spring6-3.2.5.jar"
)

for d in "${directDeps[@]}"; do
  download "$d"
done

echo ""
echo "=== 批量下载完成 ==="
echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
