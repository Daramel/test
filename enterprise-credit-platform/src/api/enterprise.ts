import request from './index'
import type { EnterpriseInfo, Shareholder, Executive } from '@/types/enterprise'

export const getEnterpriseInfo = (id: string) => {
  return request.get<EnterpriseInfo>(`/enterprise/${id}`)
}

export const getEnterpriseList = (params?: {
  page?: number
  pageSize?: number
  keyword?: string
}) => {
  return request.get<{ list: EnterpriseInfo[]; total: number }>('/enterprise/list', { params })
}

export const getShareholders = (enterpriseId: string) => {
  return request.get<Shareholder[]>(`/enterprise/${enterpriseId}/shareholders`)
}

export const getExecutives = (enterpriseId: string) => {
  return request.get<Executive[]>(`/enterprise/${enterpriseId}/executives`)
}

export const getEnterpriseCreditScore = (id: string) => {
  return request.get<{ score: number; level: string }>(`/enterprise/${id}/credit-score`)
}
