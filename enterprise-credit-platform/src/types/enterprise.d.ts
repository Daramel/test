export interface EnterpriseInfo {
  id: string
  enterpriseName: string
  creditCode: string
  legalPerson: string
  registeredCapital: string
  paidCapital: string
  establishmentDate: string
  businessTerm: string
  approvalDate: string
  registrationAuthority: string
  enterpriseStatus: string
  address: string
  businessScope: string
  creditScore?: number
  creditLevel?: string
}

export interface Shareholder {
  id: string
  name: string
  capitalContribution: string
  contributionRatio: string
}

export interface Executive {
  id: string
  name: string
  position: string
 学历: string
}

export interface Report {
  id: string
  enterpriseId: string
  enterpriseName: string
  reportType: ReportType
  reportName: string
  price: number
  status: ReportStatus
  createTime: string
  downloadUrl?: string
}

export type ReportType = 'simple' | 'standard' | 'deep'

export type ReportStatus = 'pending' | 'processing' | 'completed' | 'failed'

export interface ReportQuery {
  page?: number
  pageSize?: number
  enterpriseName?: string
  reportType?: ReportType
  status?: ReportStatus
  startDate?: string
  endDate?: string
}

export interface Order {
  id: string
  orderNo: string
  enterpriseId: string
  enterpriseName: string
  reportType: ReportType
  amount: number
  status: OrderStatus
  createTime: string
  payTime?: string
  reportId?: string
}

export type OrderStatus = 'pending' | 'paid' | 'completed' | 'cancelled' | 'refunded'

export interface OrderQuery {
  page?: number
  pageSize?: number
  status?: OrderStatus
  startDate?: string
  endDate?: string
}
