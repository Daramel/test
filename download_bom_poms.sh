#!/bin/bash

REPO_URL="https://maven.aliyun.com/repository/public"
LOCAL_REPO="/root/.m2/repository"

# 需要下载的 BOM 列表
BOMS=(
    "io.dropwizard.metrics:metrics-bom:4.2.25"
    "org.glassfish.jaxb:jaxb-bom:4.0.5"
    "org.apache.groovy:groovy-bom:4.0.21"
    "org.infinispan:infinispan-bom:14.0.27.Final"
    "com.fasterxml.jackson:jackson-bom:2.15.4"
    "org.glassfish.jersey:jersey-bom:3.1.6"
    "org.eclipse.jetty.ee10:jetty-ee10-bom:12.0.8"
    "org.eclipse.jetty:jetty-bom:12.0.8"
    "org.junit:junit-bom:5.10.2"
    "org.jetbrains.kotlin:kotlin-bom:1.9.23"
    "org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.7.3"
    "org.jetbrains.kotlinx:kotlinx-serialization-bom:1.6.3"
    "org.apache.logging.log4j:log4j-bom:2.21.1"
    "io.micrometer:micrometer-bom:1.12.5"
    "io.micrometer:micrometer-tracing-bom:1.2.5"
    "org.mockito:mockito-bom:5.7.0"
    "io.netty:netty-bom:4.1.109.Final"
    "com.squareup.okhttp3:okhttp-bom:4.12.0"
    "io.opentelemetry:opentelemetry-bom:1.31.0"
    "com.oracle.database.jdbc:ojdbc-bom:21.9.0.0"
    "io.prometheus:simpleclient_bom:0.16.0"
    "com.querydsl:querydsl-bom:5.0.0"
    "io.projectreactor:reactor-bom:2023.0.5"
    "io.rest-assured:rest-assured-bom:5.3.2"
    "io.rsocket:rsocket-bom:1.1.3"
    "org.seleniumhq.selenium:selenium-bom:4.14.1"
    "org.springframework.amqp:spring-amqp-bom:3.1.4"
    "org.springframework.batch:spring-batch-bom:5.1.1"
    "org.springframework.data:spring-data-bom:2023.1.5"
    "org.springframework:spring-framework-bom:6.1.6"
    "org.springframework.integration:spring-integration-bom:6.2.4"
    "org.springframework.pulsar:spring-pulsar-bom:1.0.5"
    "org.springframework.restdocs:spring-restdocs-bom:3.0.1"
    "org.springframework.security:spring-security-bom:6.2.4"
    "org.springframework.session:spring-session-bom:3.2.2"
    "org.springframework.ws:spring-ws-bom:4.0.10"
    "org.testcontainers:testcontainers-bom:1.19.7"
)

download_pom() {
    local gav=$1
    local group_id=$(echo $gav | cut -d: -f1)
    local artifact_id=$(echo $gav | cut -d: -f2)
    local version=$(echo $gav | cut -d: -f3)
    
    local group_path=$(echo $group_id | tr '.' '/')
    local pom_dir="$LOCAL_REPO/$group_path/$artifact_id/$version"
    local pom_file="$pom_dir/$artifact_id-$version.pom"
    
    if [ -f "$pom_file" ] && [ -s "$pom_file" ]; then
        echo "✓ 已存在: $gav"
        return 0
    fi
    
    mkdir -p "$pom_dir"
    
    local url="$REPO_URL/$group_path/$artifact_id/$version/$artifact_id-$version.pom"
    echo "↓ 下载: $gav"
    
    if wget -q --timeout=30 --tries=3 -O "$pom_file" "$url" 2>/dev/null; then
        rm -f "$pom_file.lastUpdated"
        echo "✓ 成功: $gav"
        return 0
    else
        echo "✗ 失败: $gav"
        rm -f "$pom_file"
        return 1
    fi
}

echo "=== 批量下载 BOM POM 文件 ==="
success=0
fail=0

for bom in "${BOMS[@]}"; do
    if download_pom "$bom"; then
        success=$((success + 1))
    else
        fail=$((fail + 1))
    fi
done

echo ""
echo "=== 下载完成 ==="
echo "成功: $success, 失败: $fail"
