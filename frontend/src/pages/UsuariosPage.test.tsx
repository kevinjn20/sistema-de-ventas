import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import UsuariosPage from './UsuariosPage'
import { usuarioService } from '../services/usuarioService'

vi.mock('../services/usuarioService')

const mockUsuarios = [
  { id: 1, username: 'admin', nombre: 'Admin', rol: 'ADMIN', activo: true, createdAt: '2024-01-01T00:00:00', updatedAt: '2024-01-01T00:00:00' },
]

beforeEach(() => {
  vi.clearAllMocks()
})

describe('UsuariosPage', () => {
  it('renderiza el titulo y el boton de nuevo usuario', async () => {
    vi.mocked(usuarioService.listar).mockResolvedValue([])

    render(
      <MemoryRouter>
        <UsuariosPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('Usuarios')).toBeInTheDocument()
    expect(screen.getByText('+ Nuevo Usuario')).toBeInTheDocument()
  })

  it('muestra los usuarios cargados', async () => {
    vi.mocked(usuarioService.listar).mockResolvedValue(mockUsuarios)

    render(
      <MemoryRouter>
        <UsuariosPage />
      </MemoryRouter>
    )

    expect(await screen.findByText('admin')).toBeInTheDocument()
    expect(screen.getByText('ADMIN')).toBeInTheDocument()
  })
})
