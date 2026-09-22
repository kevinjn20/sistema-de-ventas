import { api } from './api'

export interface Usuario {
  id: number
  username: string
  nombre: string | null
  rol: string
  activo: boolean
  createdAt: string
  updatedAt: string
}

export interface UsuarioRequest {
  username: string
  password?: string
  nombre?: string
  rol?: string
}

const API_URL = '/api/usuarios'

export const usuarioService = {
  async listar(): Promise<Usuario[]> {
    const { data } = await api.get<Usuario[]>(API_URL)
    return data
  },

  async obtener(id: number): Promise<Usuario> {
    const { data } = await api.get<Usuario>(`${API_URL}/${id}`)
    return data
  },

  async crear(request: UsuarioRequest): Promise<Usuario> {
    const { data } = await api.post<Usuario>(API_URL, request)
    return data
  },

  async actualizar(id: number, request: UsuarioRequest): Promise<Usuario> {
    const { data } = await api.put<Usuario>(`${API_URL}/${id}`, request)
    return data
  },

  async eliminar(id: number): Promise<void> {
    await api.delete(`${API_URL}/${id}`)
  },
}
