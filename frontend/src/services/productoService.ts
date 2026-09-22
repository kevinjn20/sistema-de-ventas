import { api } from './api'

export interface Producto {
  id: number
  nombre: string
  descripcion: string | null
  precio: number
  stock: number
  activo: boolean
  createdAt: string
  updatedAt: string
}

export interface ProductoRequest {
  nombre: string
  descripcion?: string
  precio: number
  stock: number
}

const API_URL = '/api/productos'

export const productoService = {
  async listar(): Promise<Producto[]> {
    const { data } = await api.get<Producto[]>(API_URL)
    return data
  },

  async obtener(id: number): Promise<Producto> {
    const { data } = await api.get<Producto>(`${API_URL}/${id}`)
    return data
  },

  async crear(request: ProductoRequest): Promise<Producto> {
    const { data } = await api.post<Producto>(API_URL, request)
    return data
  },

  async actualizar(id: number, request: ProductoRequest): Promise<Producto> {
    const { data } = await api.put<Producto>(`${API_URL}/${id}`, request)
    return data
  },

  async eliminar(id: number): Promise<void> {
    await api.delete(`${API_URL}/${id}`)
  },
}
