#!/bin/bash

REPO_DIR="/root/.m2/repository"

echo "移除非 Spring Boot POM 文件中的 dependencyManagement 部分..."
echo "这可以大大减少需要下载的 BOM 依赖数量"
echo ""

count=0
total=0

while IFS= read -r pomfile; do
  total=$((total + 1))
  
  # 跳过 Spring Boot 官方的 POM（我们需要它们的版本管理）
  if echo "$pomfile" | grep -q 'spring-boot'; then
    continue
  fi
  
  # 跳过 spring-framework 的 POM
  if echo "$pomfile" | grep -q 'spring-framework'; then
    continue
  fi
  
  # 跳过我们自己项目的 POM
  if echo "$pomfile" | grep -q 'credit-platform'; then
    continue
  fi
  
  if grep -q '<dependencyManagement>' "$pomfile"; then
    # 备份
    cp "$pomfile" "${pomfile}.bak" 2>/dev/null
    # 移除 dependencyManagement 部分
    sed -i '/<dependencyManagement>/,/<\/dependencyManagement>/d' "$pomfile"
    count=$((count + 1))
    echo "  已处理: $pomfile"
  fi
done < <(find "$REPO_DIR" -name "*.pom" -type f)

echo ""
echo "处理完成！共检查 $total 个 POM，处理了 $count 个"
echo "仓库大小: $(du -sh "$REPO_DIR" 2>/dev/null | cut -f1)"
