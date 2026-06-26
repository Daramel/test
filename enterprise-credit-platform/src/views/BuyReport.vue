<template>
  <div class="buy-report-container">
    <div class="page-header">
      <h2 class="page-title">购买报告</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <div class="card-container">
          <h3 class="card-title">选择报告类型</h3>
          <div class="report-types">
            <div
              v-for="item in reportTypes"
              :key="item.type"
              class="report-type-card"
              :class="{ active: selectedType === item.type }"
              @click="selectedType = item.type"
            >
              <div class="type-header">
                <span class="type-name">{{ item.name }}</span>
                <span class="type-price">{{ formatMoney(item.price) }}</span>
              </div>
              <div class="type-desc">
                {{ getTypeDescription(item.type) }}
              </div>
              <div class="type-features">
                <div class="feature-item">
                  <el-icon><Check /></el-icon>
                  <span>基本信息查询</span>
                </div>
                <div class="feature-item">
                  <el-icon><Check /></el-icon>
                  <span>工商信息查询</span>
                </div>
                <div class="feature-item" :class="{ disabled: item.type === 'simple' }">
                  <el-icon><Check /></el-icon>
                  <span>股东信息查询</span>
                </div>
                <div class="feature-item" :class="{ disabled: item.type !== 'deep' }">
                  <el-icon><Check /></el-icon>
                  <span>深度数据分析</span>
                </div>
              </div>
              <div v-if="selectedType === item.type" class="selected-indicator">
                <el-icon><Check /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <div class="card-container mt-20">
          <h3 class="card-title">填写企业信息</h3>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
            <el-form-item label="目标企业" prop="enterpriseName">
              <el-input v-model="form.enterpriseName" placeholder="请输入企业名称" />
            </el-form-item>
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" maxlength="18" />
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="card-container order-summary">
          <h3 class="card-title">订单摘要</h3>
          <div class="summary-content">
            <div class="summary-item">
              <span class="label">报告类型</span>
              <span class="value">{{ selectedTypeName }}</span>
            </div>
            <div class="summary-item">
              <span class="label">企业名称</span>
              <span class="value">{{ form.enterpriseName || '-' }}</span>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-item total">
              <span class="label">订单金额</span>
              <span class="value price">{{ selectedPrice }}</span>
            </div>
          </div>
          <el-button
            type="primary"
            size="large"
            class="buy-button"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交订单
          </el-button>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getReportTypes } from '@/api/report'
import { createOrder } from '@/api/order'
import { formatMoney, validateCreditCode } from '@/utils/common'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const reportTypes = ref<{ type: string; name: string; price: number }[]>([])
const selectedType = ref('standard')

const form = reactive({
  enterpriseName: '',
  creditCode: ''
})

const rules: FormRules = {
  enterpriseName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (!validateCreditCode(value)) {
        callback(new Error('请输入正确的统一社会信用代码'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

const selectedTypeName = computed(() => {
  const item = reportTypes.value.find(r => r.type === selectedType.value)
  return item?.name || '标准版'
})

const selectedPrice = computed(() => {
  const item = reportTypes.value.find(r => r.type === selectedType.value)
  return item ? formatMoney(item.price) : '¥0.00'
})

const getTypeDescription = (type: string): string => {
  const descriptions: Record<string, string> = {
    simple: '包含企业基本工商信息，适用于快速查询',
    standard: '包含详细工商信息、股东信息，适用于一般商务合作',
    deep: '包含深度数据分析、风险评估等，适用于投融资、并购等场景'
  }
  return descriptions[type] || ''
}

const fetchReportTypes = async () => {
  try {
    const types = await getReportTypes()
    reportTypes.value = types
  } catch (error) {
    console.error('Failed to fetch report types:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const order = await createOrder({
        enterpriseId: form.creditCode,
        reportType: selectedType.value
      })

      ElMessage.success('订单创建成功')
      router.push({ path: '/order', query: { id: order.id } })
    } catch (error) {
      console.error('Failed to create order:', error)
      ElMessage.error('订单创建失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  fetchReportTypes()

  if (route.query.type) {
    selectedType.value = route.query.type as string
  }
})
</script>

<style lang="scss" scoped>
.buy-report-container {
  .page-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }

  .card-container {
    background-color: #fff;
    border-radius: 4px;
    padding: 20px;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
    }
  }

  .mt-20 {
    margin-top: 20px;
  }

  .report-types {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    .report-type-card {
      border: 2px solid #ebeef5;
      border-radius: 8px;
      padding: 20px;
      cursor: pointer;
      transition: all 0.3s;
      position: relative;

      &:hover {
        border-color: #c0c4cc;
      }

      &.active {
        border-color: #409eff;
        background-color: #ecf5ff;
      }

      .type-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .type-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
        }

        .type-price {
          font-size: 18px;
          font-weight: 600;
          color: #f56c6c;
        }
      }

      .type-desc {
        font-size: 13px;
        color: #909399;
        margin-bottom: 16px;
        line-height: 1.5;
      }

      .type-features {
        .feature-item {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #606266;
          margin-bottom: 8px;

          .el-icon {
            color: #67c23a;
          }

          &.disabled {
            color: #c0c4cc;

            .el-icon {
              color: #c0c4cc;
            }
          }
        }
      }

      .selected-indicator {
        position: absolute;
        top: -12px;
        right: -12px;
        width: 28px;
        height: 28px;
        background-color: #409eff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }
    }
  }

  .order-summary {
    position: sticky;
    top: 20px;

    .summary-content {
      margin-bottom: 20px;

      .summary-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .label {
          color: #909399;
          font-size: 14px;
        }

        .value {
          color: #303133;
          font-size: 14px;

          &.price {
            font-size: 24px;
            font-weight: 600;
            color: #f56c6c;
          }
        }

        &.total {
          margin-bottom: 0;
        }
      }

      .summary-divider {
        height: 1px;
        background-color: #ebeef5;
        margin: 16px 0;
      }
    }

    .buy-button {
      width: 100%;
    }
  }
}
</style>
