import request from './index'
import type { LoginDTO, LoginVO, RegisterDTO } from '@/types/user'

export const login = (data: LoginDTO) => {
  return request.post<LoginVO>('/auth/login', data)
}

export const register = (data: RegisterDTO) => {
  return request.post<void>('/auth/register', data)
}

export const logout = () => {
  return request.post<void>('/auth/logout')
}

export const getUserInfo = () => {
  return request.get<LoginVO>('/auth/userinfo')
}

export const sendSmsCode = (phone: string, type: string) => {
  return request.post<void>('/auth/sms/send', { phone, type })
}

export const resetPassword = (phone: string, newPassword: string, smsCode: string) => {
  return request.post<void>('/auth/reset-password', { phone, newPassword, smsCode })
}
