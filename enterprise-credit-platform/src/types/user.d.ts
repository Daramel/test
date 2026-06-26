export interface LoginDTO {
  phone: string
  password: string
  rememberMe?: boolean
}

export interface LoginVO {
  token: string
  userId: string
  phone: string
  nickname: string
  avatar?: string
  permissions: string[]
}

export interface RegisterDTO {
  enterpriseName: string
  creditCode: string
  legalPerson: string
  phone: string
  password: string
  smsCode: string
}

export interface UserInfo {
  userId: string
  phone: string
  nickname: string
  avatar?: string
  email?: string
  permissions: string[]
  createTime: string
}
