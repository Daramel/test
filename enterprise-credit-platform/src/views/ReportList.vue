<template>
  <div class="report-list-container">
    <div class="page-header">
      <h2 class="page-title">报告管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="card-container filter-section">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="企业名称">
          <el-input v-model="filterForm.enterpriseName" placeholder="请输入企业名称" clearable />
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="filterForm.reportType" placeholder="请选择" clearable>
            <el-option label="简版" value="simple" />
            <el-option label="标准版" value="standard" />
            <el-option label="深度版" value="deep" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已完成" value="completed" />
            <el-option label="失败" value="failed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="card-container table-section">
      <el-table :data="reportList" v-loading="loading" style="width: 100%">
        <el-table-column prop="reportName" label="报告名称" min-width="200" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="150" />
        <el-table-column prop="reportType" label="报告类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getReportTypeTag(row.reportType)">
              {{ getReportTypeName(row.reportType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            {{ formatMoney(row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
            <el-button
              type="success"
              link
              size="small"
              :disabled="row.status !== 'completed'"
              @click="handleDownload(row)"
            >
              下载
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog v-model="viewDialogVisible" title="报告详情" width="800px">
      <div v-if="currentReport" class="report-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报告名称">{{ currentReport.reportName }}</el-descriptions-item>
          <el-descriptions-item label="企业名称">{{ currentReport.enterpriseName }}</el-descriptions-item>
          <el-descriptions-item label="报告类型">{{ getReportTypeName(currentReport.reportType) }}</el-descriptions-item>
          <el-descriptions-item label="价格">{{ formatMoney(currentReport.price) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ getStatusName(currentReport.status) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentReport.createTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getReportList, downloadReport } from '@/api/report'
import { formatMoney, downloadFile } from '@/utils/common'
import type { Report, ReportQuery } from '@/types/enterprise'

const loading = ref(false)
const viewDialogVisible = ref(false)
const currentReport = ref<Report | null>(null)

const filterForm = reactive<ReportQuery>({
  enterpriseName: '',
  reportType: undefined,
  status: undefined
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const reportList = ref<Report[]>([])

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

const getStatusName = (status: string): string => {
  const map: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    failed: '失败'
  }
  return map[status] || status
}

const getStatusTag = (status: string): string => {
  const map: Record<string, string> = {
    pending: 'warning',
    processing: 'primary',
    completed: 'success',
    failed: 'danger'
  }
  return map[status] || 'info'
}

const fetchReportList = async () => {
  loading.value = true
  try {
    const params: ReportQuery = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filterForm
    }
    const res = await getReportList(params)
    reportList.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('Failed to fetch report list:', error)
    ElMessage.error('获取报告列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchReportList()
}

const handleReset = () => {
  filterForm.enterpriseName = ''
  filterForm.reportType = undefined
  filterForm.status = undefined
  pagination.page = 1
  fetchReportList()
}

const handleRefresh = () => {
  fetchReportList()
  ElMessage.success('已刷新')
}

const handleView = (row: Report) => {
  currentReport.value = row
  viewDialogVisible.value = true
}

const handleDownload = async (row: Report) => {
  try {
    const url = await downloadReport(row.id)
    downloadFile(url, `${row.reportName}.pdf`)
    ElMessage.success('下载开始')
  } catch (error) {
    console.error('Download failed:', error)
    ElMessage.error('下载失败')
  }
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchReportList()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchReportList()
}

onMounted(() => {
  fetchReportList()
})
</script>

<style lang="scss" scoped>
.report-list-container {
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

  .filter-section {
    .filter-form {
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }

  .table-section {
    .pagination-wrapper {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }

  .report-detail {
    padding: 10px;
  }
}
</style>
