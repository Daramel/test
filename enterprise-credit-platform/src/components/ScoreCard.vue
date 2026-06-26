<template>
  <div class="score-card" :class="levelClass">
    <div class="card-header">
      <span class="card-title">信用评分</span>
      <el-tag v-if="level" :type="levelTagType" size="small">{{ level }}</el-tag>
    </div>
    <div class="score-display">
      <div class="score-value">{{ score }}</div>
      <div class="score-max">/ 1000</div>
    </div>
    <div class="score-progress">
      <el-progress
        :percentage="percentage"
        :color="progressColor"
        :show-text="false"
        :stroke-width="8"
      />
    </div>
    <div class="score-range">
      <span class="range-item">
        <span class="range-label">低</span>
        <span class="range-value">0-400</span>
      </span>
      <span class="range-item">
        <span class="range-label">中</span>
        <span class="range-value">400-700</span>
      </span>
      <span class="range-item">
        <span class="range-label">高</span>
        <span class="range-value">700-1000</span>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  score: number
  level?: string
}>()

const percentage = computed(() => Math.min((props.score / 1000) * 100, 100))

const levelClass = computed(() => {
  if (props.score >= 700) return 'high'
  if (props.score >= 400) return 'medium'
  return 'low'
})

const progressColor = computed(() => {
  if (props.score >= 700) return '#67c23a'
  if (props.score >= 400) return '#e6a23c'
  return '#f56c6c'
})

const levelTagType = computed(() => {
  if (props.score >= 700) return 'success'
  if (props.score >= 400) return 'warning'
  return 'danger'
})
</script>

<style lang="scss" scoped>
.score-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .card-title {
      font-size: 14px;
      color: #909399;
    }
  }

  .score-display {
    display: flex;
    align-items: baseline;
    margin-bottom: 16px;

    .score-value {
      font-size: 48px;
      font-weight: 700;
      color: #303133;
    }

    .score-max {
      font-size: 16px;
      color: #c0c4cc;
      margin-left: 4px;
    }
  }

  .score-progress {
    margin-bottom: 16px;
  }

  .score-range {
    display: flex;
    justify-content: space-between;

    .range-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .range-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 2px;
      }

      .range-value {
        font-size: 12px;
        color: #606266;
      }
    }
  }

  &.high {
    border-left: 4px solid #67c23a;
  }

  &.medium {
    border-left: 4px solid #e6a23c;
  }

  &.low {
    border-left: 4px solid #f56c6c;
  }
}
</style>
