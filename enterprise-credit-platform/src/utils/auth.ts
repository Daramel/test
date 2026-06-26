const TOKEN_KEY = 'auth_token'

export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

export const getRememberMe = (): boolean => {
  return localStorage.getItem('remember_me') === 'true'
}

export const setRememberMe = (remember: boolean): void => {
  localStorage.setItem('remember_me', String(remember))
}
