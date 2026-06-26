<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h2 class="welcome-title">欢迎回来，{{ userInfo?.nickname || '用户' }}</h2>
      <p class="welcome-desc">以下是您的企业征信概览信息</p>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #409eff;">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.reportCount }}</div>
            <div class="stat-label">报告总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #67c23a;">
            <el-icon :size="24"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #e6a23c;">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.creditScore || '--' }}</div>
            <div class="stat-label">信用评分</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background-color: #f56c6c;">
            <el-icon :size="24"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingOrders }}</div>
            <div class="stat-label">待处理订单</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :span="16">
        <div class="card-container enterprise-card">
          <div class="card-header">
            <h3 class="card-title">企业信息</h3>
            <router-link to="/enterprise" class="more-link">查看详情</router-link>
          </div>
          <div class="enterprise-content" v-if="enterpriseInfo">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="企业名称">{{ enterpriseInfo.enterpriseName }}</el-descriptions-item>
              <el-descriptions-item label="统一社会信用代码">{{ enterpriseInfo.creditCode }}</el-descriptions-item>
              <el-descriptions-item label="法定代表人">{{ enterpriseInfo.legalPerson }}</el-descriptions-item>
              <el-descriptions-item label="注册资本">{{ enterpriseInfo.registeredCapital }}</el-descriptions-item>
              <el-descriptions-item label="成立日期">{{ enterpriseInfo.establishmentDate }}</el-descriptions-item>
              <el-descriptions-item label="企业状态">{{ enterpriseInfo.enterpriseStatus }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <EmptyData v-else description="暂无企业信息" />
        </div>

        <div class="card-container">
          <div class="card-header">
            <h3 class="card-title">最近报告</h3>
            <router-link to="/report" class="more-link">查看更多</router-link>
          </div>
          <el-table :data="recentReports" style="width: 100%">
            <el-table-column prop="reportName" label="报告名称" />
            <el-table-column prop="reportType" label="报告类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getReportTypeTag(row.reportType)">{{ getReportTypeName(row.reportType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link size="small">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card-container quick-actions">
          <div class="card-header">
            <h3 class="card-title">快捷操作</h3>
          </div>
          <div class="action-list">
            <div class="action-item" @click="router.push('/report')">
              <el-icon :size="24"><DocumentAdd /></el-icon>
              <span>购买报告</span>
            </div>
            <div class="action-item" @click="router.push('/enterprise')">
              <el-icon :size="24"><OfficeBuilding /></el-icon>
              <span>企业管理</span>
            </div>
            <div class="action-item" @click="router.push('/order')">
              <el-icon :size="24"><List /></el-icon>
              <span>订单管理</span>
            </div>
            <div class="action-item" @click="router.push('/authorization')">
              <el-icon :size="24"><Key /></el-icon>
              <span>授权管理</span>
            </div>
          </div>
        </div>

        <ScoreCard v-if="enterpriseInfo?.creditScore" :score="enterpriseInfo.creditScore" class="mt-20" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { Document, SuccessFilled, Star, Clock, DocumentAdd, OfficeBuilding, List, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import EmptyData from '@/components/EmptyData.vue'
import ScoreCard from '@/components/ScoreCard.vue'
import type { EnterpriseInfo, Report } from '@/types/enterprise'
import { getEnterpriseInfo, getEnterpriseCreditScore } from '@/api/enterprise'
import { getReportList } from '@/api/report'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
const enterpriseInfo = ref<EnterpriseInfo | null>(null)
const recentReports = ref<Report[]>([])

const stats = reactive({
  reportCount: 0,
  orderCount: 0,
  creditScore: null as number | null,
  pendingOrders: 0
})

const getReportTypeName = (type: string): string => {
  const map: Record<string, string> = {
    simple: '简版',
    standard: '标准版',
    deep: '深度版'
  }
  return map[type] || type
}

const getReportTypeTag = (type: string): string => {
  const map: Record<string, string> = {
    simple: 'info',
    standard: 'primary',
    deep: 'success'
  }
  return map[type] || 'info'
}

const fetchDashboardData = async () => {
  try {
    const [reportRes, enterpriseRes] = await Promise.all([
      getReportList({ page: 1, pageSize: 5 }),
      getEnterpriseInfo('1').catch(() => null)
    ])

    recentReports.value = reportRes.list
    stats.reportCount = reportRes.total

    if (enterpriseRes) {
      enterpriseInfo.value = enterpriseRes
      const scoreRes = await getEnterpriseCreditScore(enterpriseRes.id).catch(() => null)
      if (scoreRes) {
        stats.creditScore = scoreRes.score
      }
    }
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 0;
}

.welcome-section {
  margin-bottom: 24px;

  .welcome-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .welcome-desc {
    color: #909399;
    font-size: 14px;
  }
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin-right: 16px;
  }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.content-row {
  .card-container {
    background-color: #fff;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 16px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .more-link {
      font-size: 14px;
      color: #409eff;
    }
  }
}

.quick-actions {
  .action-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .action-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px;
      background-color: #f5f7fa;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background-color: #ecf5ff;
        color: #409eff;
      }

      span {
        margin-top: 8px;
        font-size: 14px;
        color: #606266;
      }

      &:hover span {
        color: #409eff;
      }
    }
  }
}

.mt-20 {
  margin-top: 20px;
}
</style>
