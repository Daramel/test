<template>
  <div class="enterprise-info-container">
    <div class="page-header">
      <h2 class="page-title">企业信息</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleRefresh">刷新数据</el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-icon class="loading-icon" :size="32"><Loading /></el-icon>
      <span>加载中...</span>
    </div>

    <div v-else-if="enterpriseInfo" class="info-content">
      <el-row :gutter="20">
        <el-col :span="16">
          <div class="card-container basic-info">
            <h3 class="card-title">基本信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="企业名称" :span="2">
                {{ enterpriseInfo.enterpriseName }}
              </el-descriptions-item>
              <el-descriptions-item label="统一社会信用代码">
                {{ enterpriseInfo.creditCode }}
              </el-descriptions-item>
              <el-descriptions-item label="法定代表人">
                {{ enterpriseInfo.legalPerson }}
              </el-descriptions-item>
              <el-descriptions-item label="注册资本">
                {{ enterpriseInfo.registeredCapital }}
              </el-descriptions-item>
              <el-descriptions-item label="实缴资本">
                {{ enterpriseInfo.paidCapital }}
              </el-descriptions-item>
              <el-descriptions-item label="成立日期">
                {{ enterpriseInfo.establishmentDate }}
              </el-descriptions-item>
              <el-descriptions-item label="营业期限">
                {{ enterpriseInfo.businessTerm }}
              </el-descriptions-item>
              <el-descriptions-item label="核准日期">
                {{ enterpriseInfo.approvalDate }}
              </el-descriptions-item>
              <el-descriptions-item label="登记机关">
                {{ enterpriseInfo.registrationAuthority }}
              </el-descriptions-item>
              <el-descriptions-item label="企业状态">
                <el-tag :type="getStatusType(enterpriseInfo.enterpriseStatus)">
                  {{ enterpriseInfo.enterpriseStatus }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="企业地址" :span="2">
                {{ enterpriseInfo.address }}
              </el-descriptions-item>
              <el-descriptions-item label="经营范围" :span="2">
                {{ enterpriseInfo.businessScope }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="card-container mt-20">
            <h3 class="card-title">股东信息</h3>
            <el-table :data="shareholders" style="width: 100%">
              <el-table-column prop="name" label="股东名称" />
              <el-table-column prop="capitalContribution" label="认缴出资额" />
              <el-table-column prop="contributionRatio" label="出资比例" />
            </el-table>
            <EmptyData v-if="shareholders.length === 0" description="暂无股东信息" />
          </div>

          <div class="card-container mt-20">
            <h3 class="card-title">高管信息</h3>
            <el-table :data="executives" style="width: 100%">
              <el-table-column prop="name" label="姓名" />
              <el-table-column prop="position" label="职务" />
              <el-table-column prop="学历" label="学历" />
            </el-table>
            <EmptyData v-if="executives.length === 0" description="暂无高管信息" />
          </div>
        </el-col>

        <el-col :span="8">
          <ScoreCard
            v-if="enterpriseInfo.creditScore"
            :score="enterpriseInfo.creditScore"
            :level="enterpriseInfo.creditLevel"
          />

          <div class="card-container mt-20">
            <h3 class="card-title">信用报告</h3>
            <div class="report-actions">
              <el-button type="primary" @click="handleBuyReport('simple')">购买简版报告</el-button>
              <el-button type="success" @click="handleBuyReport('standard')">购买标准版报告</el-button>
              <el-button type="warning" @click="handleBuyReport('deep')">购买深度版报告</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <EmptyData v-else description="暂无企业信息" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import EmptyData from '@/components/EmptyData.vue'
import ScoreCard from '@/components/ScoreCard.vue'
import type { EnterpriseInfo, Shareholder, Executive } from '@/types/enterprise'
import { getEnterpriseInfo, getShareholders, getExecutives } from '@/api/enterprise'

const router = useRouter()
const loading = ref(false)
const enterpriseInfo = ref<EnterpriseInfo | null>(null)
const shareholders = ref<Shareholder[]>([])
const executives = ref<Executive[]>([])

const getStatusType = (status: string): string => {
  const map: Record<string, string> = {
    '存续': 'success',
    '在业': 'success',
    '吊销': 'danger',
    '注销': 'info',
    '迁入': 'warning',
    '迁出': 'warning'
  }
  return map[status] || 'info'
}

const handleBuyReport = (type: string) => {
  router.push({ path: '/order', query: { type } })
}

const fetchData = async () => {
  loading.value = true
  try {
    const enterpriseId = '1'
    const [enterprise, shareholdersData, executivesData] = await Promise.all([
      getEnterpriseInfo(enterpriseId).catch(() => null),
      getShareholders(enterpriseId).catch(() => []),
      getExecutives(enterpriseId).catch(() => [])
    ])

    enterpriseInfo.value = enterprise
    shareholders.value = shareholdersData
    executives.value = executivesData
  } catch (error) {
    console.error('Failed to fetch enterprise info:', error)
    ElMessage.error('获取企业信息失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  fetchData()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.enterprise-info-container {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60px;
    color: #909399;

    .loading-icon {
      animation: rotating 2s linear infinite;
      margin-right: 8px;
    }
  }

  .info-content {
    .card-container {
      background-color: #fff;
      border-radius: 4px;
      padding: 20px;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 16px;
        padding-bottom: 12px;
        border-bottom: 1px solid #ebeef5;
      }
    }

    .mt-20 {
      margin-top: 20px;
    }

    .report-actions {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .el-button {
        width: 100%;
      }
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
