import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import DashboardPage from './DashboardPage'
import { dashboardService, Dashboard } from '../services/dashboardService'

vi.mock('../services/dashboardService')

const mockDashboard: Dashboard = {
  totalVentas: 10,
  ingresosTotales: 5000,
  clientesActivos: 25,
  productosActivos: 50,
  usuariosActivos: 5,
  ventasPorDia: [
    { fecha: '2026-07-01', total: 1500, cantidad: 3 },
    { fecha: '2026-07-02', total: 3500, cantidad: 7 },
  ],
  topProductos: [
    { productoId: 1, nombre: 'Producto A', cantidadVendida: 20, totalGenerado: 2000 },
    { productoId: 2, nombre: 'Producto B', cantidadVendida: 15, totalGenerado: 1500 },
  ],
  productosBajoStock: [{ id: 3, nombre: 'Producto C', stock: 2 }],
}

beforeEach(() => {
  vi.clearAllMocks()
})

describe('DashboardPage', () => {
  it('renderiza el titulo y las tarjetas de KPI', async () => {
    vi.mocked(dashboardService.obtener).mockResolvedValue(mockDashboard)

    render(<DashboardPage />)

    expect(await screen.findByText('Dashboard')).toBeInTheDocument()
    expect(screen.getByText('10')).toBeInTheDocument()
    expect(screen.getByText('$5,000')).toBeInTheDocument()
    expect(screen.getByText('25')).toBeInTheDocument()
    expect(screen.getByText('5')).toBeInTheDocument()
  })

  it('muestra la tabla de productos con stock bajo', async () => {
    vi.mocked(dashboardService.obtener).mockResolvedValue(mockDashboard)

    render(<DashboardPage />)

    expect(await screen.findByText('Producto C')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
  })

  it('muestra mensaje de carga inicialmente', () => {
    vi.mocked(dashboardService.obtener).mockResolvedValue(mockDashboard)

    render(<DashboardPage />)

    expect(screen.getByText('Cargando dashboard...')).toBeInTheDocument()
  })

  it('muestra mensaje de error cuando falla la peticion', async () => {
    vi.mocked(dashboardService.obtener).mockRejectedValue(new Error('Error'))

    render(<DashboardPage />)

    expect(await screen.findByText('Error al cargar el dashboard')).toBeInTheDocument()
  })

  it('muestra "Sin datos" cuando no hay ventas por dia', async () => {
    const emptyDashboard = { ...mockDashboard, ventasPorDia: [] }
    vi.mocked(dashboardService.obtener).mockResolvedValue(emptyDashboard)

    render(<DashboardPage />)

    expect(await screen.findByText('Sin datos')).toBeInTheDocument()
  })
})
