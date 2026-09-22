import { api } from './api'

export interface VentaDetalle {
  id: number
  productoId: number
  productoNombre: string
  cantidad: number
  precioUnitario: number
  subtotal: number
}

export interface Venta {
  id: number
  fecha: string
  total: number
  activo: boolean
  cliente: { id: number; nombre: string }
  usuario: { id: number; username: string }
  detalles: VentaDetalle[]
  createdAt: string
  updatedAt: string
}

export interface VentaRequest {
  clienteId: number
  usuarioId: number
  items: { productoId: number; cantidad: number }[]
}

const API_URL = '/api/ventas'

export const ventaService = {
  async listar(): Promise<Venta[]> {
    const { data } = await api.get<Venta[]>(API_URL)
    return data
  },

  async obtener(id: number): Promise<Venta> {
    const { data } = await api.get<Venta>(`${API_URL}/${id}`)
    return data
  },

  async crear(request: VentaRequest): Promise<Venta> {
    const { data } = await api.post<Venta>(API_URL, request)
    return data
  },

  async anular(id: number): Promise<void> {
    await api.delete(`${API_URL}/${id}`)
  },

  async listarPorRango(from: string, to: string): Promise<Venta[]> {
    const { data } = await api.get<Venta[]>(API_URL, { params: { from, to } })
    return data
  },
}
