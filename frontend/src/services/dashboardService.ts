import { api } from './api'

export interface VentaDiaria {
  fecha: string
  total: number
  cantidad: number
}

export interface TopProducto {
  productoId: number
  nombre: string
  cantidadVendida: number
  totalGenerado: number
}

export interface ProductoInfo {
  id: number
  nombre: string
  stock: number
}

export interface Dashboard {
  totalVentas: number
  ingresosTotales: number
  clientesActivos: number
  productosActivos: number
  usuariosActivos: number
  ventasPorDia: VentaDiaria[]
  topProductos: TopProducto[]
  productosBajoStock: ProductoInfo[]
}

const API_URL = '/api/dashboard'

export const dashboardService = {
  async obtener(from?: string, to?: string): Promise<Dashboard> {
    const { data } = await api.get<Dashboard>(API_URL, {
      params: { ...(from ? { from } : {}), ...(to ? { to } : {}) },
    })
    return data
  },
}
