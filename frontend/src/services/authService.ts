import { api, setToken, clearToken } from './api'

export interface LoginResponse {
  token: string
  tokenType: string
  username: string
  rol: string
  userId: number
}

export interface SessionUser {
  username: string
  rol: string
  userId: number
}

const USER_KEY = 'sv_user'

export function getStoredUser(): SessionUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? (JSON.parse(raw) as SessionUser) : null
  } catch {
    return null
  }
}

export const authService = {
  async login(username: string, password: string): Promise<SessionUser> {
    const { data } = await api.post<LoginResponse>('/api/auth/login', { username, password })
    setToken(data.token)
    const user: SessionUser = { username: data.username, rol: data.rol, userId: data.userId }
    try {
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    } catch {
      // entorno sin localStorage: ignorar
    }
    return user
  },

  logout(): void {
    clearToken()
    try {
      localStorage.removeItem(USER_KEY)
    } catch {
      // ignorar
    }
  },
}
