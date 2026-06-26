<template>
  <div class="breadcrumb-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
        {{ item.meta.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta.title && item.path !== '/dashboard')
})
</script>

<style lang="scss" scoped>
.breadcrumb-container {
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
}
</style>
