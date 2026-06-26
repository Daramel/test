<template>
  <div class="sidebar-container" :class="{ collapsed }">
    <div class="logo-container">
      <img v-if="!collapsed" src="@/assets/logo.png" alt="logo" class="logo" />
      <img v-else src="@/assets/logo-mini.png" alt="logo" class="logo-mini" />
    </div>
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :background-color="'#304156'"
      :text-color="'#bfcbd9'"
      :active-text-color="'#409eff'"
      :unique-opened="true"
      router
      class="sidebar-menu"
    >
      <el-menu-item index="/dashboard">
        <el-icon><HomeFilled /></el-icon>
        <template #title>首页仪表盘</template>
      </el-menu-item>
      <el-menu-item index="/enterprise">
        <el-icon><OfficeBuilding /></el-icon>
        <template #title>企业管理</template>
      </el-menu-item>
      <el-menu-item index="/report">
        <el-icon><Document /></el-icon>
        <template #title>报告管理</template>
      </el-menu-item>
      <el-menu-item index="/order">
        <el-icon><ShoppingCart /></el-icon>
        <template #title>订单管理</template>
      </el-menu-item>
      <el-menu-item index="/authorization">
        <el-icon><Key /></el-icon>
        <template #title>授权管理</template>
      </el-menu-item>
      <el-menu-item index="/system">
        <el-icon><Setting /></el-icon>
        <template #title>系统管理</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, OfficeBuilding, Document, ShoppingCart, Key, Setting } from '@element-plus/icons-vue'

defineProps<{
  collapsed?: boolean
}>()

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  background-color: #304156;
  transition: width 0.3s;

  &.collapsed {
    .sidebar-menu:not(.el-menu--collapse) {
      width: 64px;
    }
  }

  .logo-container {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #263445;

    .logo {
      height: 32px;
    }

    .logo-mini {
      height: 28px;
    }
  }

  .sidebar-menu {
    border-right: none;
    height: calc(100vh - 60px);
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: #4a5568;
      border-radius: 3px;
    }

    .el-menu-item {
      &:hover {
        background-color: #263445 !important;
      }

      &.is-active {
        background-color: #263445 !important;
      }
    }
  }
}
</style>
