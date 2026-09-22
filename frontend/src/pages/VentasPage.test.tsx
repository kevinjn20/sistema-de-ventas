import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import VentasPage from './VentasPage'
import { ventaService } from '../services/ventaService'

vi.mock('../services/ventaService')

beforeEach(() => {
  vi.clearAllMocks()
})

describe('VentasPage', () => {
  it('renderiza el titulo y el boton de nueva venta', async () => {
    vi.mocked(ventaService.listar).mockResolvedValue([])

    render(
      <MemoryRouter>
        <VentasPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Ventas')).toBeInTheDocument()
    expect(screen.getByText('+ Nueva Venta')).toBeInTheDocument()
  })

  it('muestra las ventas cargadas', async () => {
    const mockVentas = [
      {
        id: 1,
        fecha: '2026-07-15T12:00:00',
        total: 500,
        activo: true,
        cliente: { id: 1, nombre: 'Cliente A' },
        usuario: { id: 1, username: 'vendedor' },
        detalles: [{ id: 1, productoId: 1, productoNombre: 'Producto X', cantidad: 2, precioUnitario: 250, subtotal: 500 }],
        createdAt: '2026-07-15T12:00:00',
        updatedAt: '2026-07-15T12:00:00',
      },
    ]

    vi.mocked(ventaService.listar).mockResolvedValue(mockVentas)

    render(
      <MemoryRouter>
        <VentasPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Venta #1')).toBeInTheDocument()
    expect(screen.getByText('$500.00')).toBeInTheDocument()
    expect(screen.getByText(/Cliente A/)).toBeInTheDocument()
  })
})
