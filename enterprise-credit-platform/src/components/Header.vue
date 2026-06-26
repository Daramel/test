<template>
  <div class="header-container">
    <div class="header-left">
      <el-icon class="collapse-icon" @click="$emit('toggle-sidebar')">
        <Fold v-if="!isCollapsed" />
        <Expand v-else />
      </el-icon>
      <span class="system-title">企业征信管理平台</span>
    </div>
    <div class="header-right">
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <span class="username">{{ userInfo?.nickname || '用户' }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item command="settings">设置</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

defineProps<{
  isCollapsed?: boolean
}>()

defineEmits(['toggle-sidebar'])

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessageBox.alert('个人中心功能开发中', '提示')
      break
    case 'settings':
      ElMessageBox.alert('设置功能开发中', '提示')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await userStore.logout()
        router.push('/login')
      } catch {
        // Cancelled
      }
      break
  }
}
</script>

<style lang="scss" scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  width: 100%;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .collapse-icon {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      transition: color 0.3s;

      &:hover {
        color: #409eff;
      }
    }

    .system-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: #303133;
      }
    }
  }
}
</style>
