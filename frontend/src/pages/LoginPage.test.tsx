import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import LoginPage from './LoginPage'

describe('LoginPage', () => {
  it('renderiza el formulario de inicio de sesion', () => {
    render(<LoginPage />)

    expect(screen.getByText('Iniciar sesión')).toBeInTheDocument()
    expect(screen.getByLabelText('Usuario')).toBeInTheDocument()
    expect(screen.getByLabelText('Contraseña')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument()
  })
})
