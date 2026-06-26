import request from './index'
import type { Order, OrderQuery } from '@/types/enterprise'

export const getOrderList = (params?: OrderQuery) => {
  return request.get<{ list: Order[]; total: number }>('/order/list', { params })
}

export const getOrderDetail = (id: string) => {
  return request.get<Order>(`/order/${id}`)
}

export const createOrder = (data: {
  enterpriseId: string
  reportType: string
  reportId?: string
}) => {
  return request.post<Order>('/order/create', data)
}

export const cancelOrder = (id: string) => {
  return request.post<void>(`/order/${id}/cancel`)
}

export const payOrder = (id: string, paymentMethod: string) => {
  return request.post<void>(`/order/${id}/pay`, { paymentMethod })
}
