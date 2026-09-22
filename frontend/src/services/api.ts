import axios, { AxiosError } from 'axios'
import { toast } from 'sonner'

const TOKEN_KEY = 'sv_token'

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    // entorno sin localStorage (tests): ignorar
  }
}

export function clearToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // ignorar
  }
}

export function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; errors?: Record<string, string> } | undefined
    if (data?.message) return data.message
    if (data?.errors) {
      const first = Object.values(data.errors)[0]
      if (first) return first
    }
    if (error.response) {
      const status = error.response.status
      if (status === 401) return 'Sesión expirada o no autorizada'
      if (status === 403) return 'No tiene permisos para realizar esta acción'
      if (status === 404) return 'Recurso no encontrado'
      if (status === 409) return 'El recurso ya existe o hay un conflicto'
      if (status >= 500) return 'Error del servidor, intente más tarde'
    }
    if (error.request) return 'Sin respuesta del servidor, verifique su conexión'
  }
  if (error instanceof Error && error.message) return error.message
  return fallback
}

export const api = axios.create()

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      clearToken()
      window.dispatchEvent(new CustomEvent('sv:unauthorized'))
    } else if (!(error instanceof AxiosError && error.config?.headers?.['X-Silent-Error'])) {
      toast.error(getErrorMessage(error, 'Ocurrió un error inesperado'))
    }
    return Promise.reject(error)
  },
)
