import request from './index'
import type { Report, ReportQuery } from '@/types/enterprise'

export const getReportList = (params?: ReportQuery) => {
  return request.get<{ list: Report[]; total: number }>('/report/list', { params })
}

export const getReportDetail = (id: string) => {
  return request.get<Report>(`/report/${id}`)
}

export const downloadReport = (id: string) => {
  return request.get<string>(`/report/${id}/download`, { headers: { showLoading: true } })
}

export const createReport = (data: { enterpriseId: string; reportType: string }) => {
  return request.post<Report>('/report/create', data)
}

export const getReportTypes = () => {
  return request.get<{ type: string; name: string; price: number }[]>('/report/types')
}
