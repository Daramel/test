<template>
  <div class="empty-data">
    <div class="empty-icon">
      <el-icon :size="48"><DocumentDelete /></el-icon>
    </div>
    <div class="empty-text">{{ description || '暂无数据' }}</div>
    <div v-if="$slots.action" class="empty-action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { DocumentDelete } from '@element-plus/icons-vue'

defineProps<{
  description?: string
}>()
</script>

<style lang="scss" scoped>
.empty-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;

  .empty-icon {
    margin-bottom: 16px;
    color: #dcdfe6;
  }

  .empty-text {
    font-size: 14px;
    margin-bottom: 16px;
  }

  .empty-action {
    margin-top: 8px;
  }
}
</style>
