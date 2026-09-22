import { api } from './api'

export interface Cliente {
  id: number
  nombre: string
  email: string | null
  telefono: string | null
  direccion: string | null
  activo: boolean
  createdAt: string
  updatedAt: string
}

export interface ClienteRequest {
  nombre: string
  email?: string
  telefono?: string
  direccion?: string
}

const API_URL = '/api/clientes'

export const clienteService = {
  async listar(): Promise<Cliente[]> {
    const { data } = await api.get<Cliente[]>(API_URL)
    return data
  },

  async obtener(id: number): Promise<Cliente> {
    const { data } = await api.get<Cliente>(`${API_URL}/${id}`)
    return data
  },

  async crear(request: ClienteRequest): Promise<Cliente> {
    const { data } = await api.post<Cliente>(API_URL, request)
    return data
  },

  async actualizar(id: number, request: ClienteRequest): Promise<Cliente> {
    const { data } = await api.put<Cliente>(`${API_URL}/${id}`, request)
    return data
  },

  async eliminar(id: number): Promise<void> {
    await api.delete(`${API_URL}/${id}`)
  },
}
