import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import ClientesPage from './ClientesPage'
import { clienteService } from '../services/clienteService'

vi.mock('../services/clienteService')

const mockClientes = [
  { id: 1, nombre: 'Cliente A', email: 'a@example.com', telefono: null, direccion: null, activo: true, createdAt: '2024-01-01T00:00:00', updatedAt: '2024-01-01T00:00:00' },
]

beforeEach(() => {
  vi.clearAllMocks()
})

describe('ClientesPage', () => {
  it('renderiza el titulo y el boton de nuevo cliente', async () => {
    vi.mocked(clienteService.listar).mockResolvedValue([])

    render(
      <MemoryRouter>
        <ClientesPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Clientes')).toBeInTheDocument()
    expect(screen.getByText('+ Nuevo Cliente')).toBeInTheDocument()
  })

  it('muestra los clientes cargados', async () => {
    vi.mocked(clienteService.listar).mockResolvedValue(mockClientes)

    render(
      <MemoryRouter>
        <ClientesPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Cliente A')).toBeInTheDocument()
    expect(screen.getByText('a@example.com')).toBeInTheDocument()
  })
})
