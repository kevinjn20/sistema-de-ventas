import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ProductosPage from './ProductosPage'
import { productoService } from '../services/productoService'

vi.mock('../services/productoService')

const mockProductos = [
  { id: 1, nombre: 'Producto A', descripcion: null, precio: 100, stock: 10, activo: true, createdAt: '2024-01-01T00:00:00', updatedAt: '2024-01-01T00:00:00' },
]

beforeEach(() => {
  vi.clearAllMocks()
})

describe('ProductosPage', () => {
  it('renderiza el titulo y el boton de nuevo producto', async () => {
    vi.mocked(productoService.listar).mockResolvedValue([])

    render(
      <MemoryRouter>
        <ProductosPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Productos')).toBeInTheDocument()
    expect(screen.getByText('+ Nuevo Producto')).toBeInTheDocument()
  })

  it('muestra los productos cargados', async () => {
    vi.mocked(productoService.listar).mockResolvedValue(mockProductos)

    render(
      <MemoryRouter>
        <ProductosPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Producto A')).toBeInTheDocument()
    expect(screen.getByText('$100.00')).toBeInTheDocument()
  })
})
