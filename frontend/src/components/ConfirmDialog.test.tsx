import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import ConfirmDialog from './ConfirmDialog'

describe('ConfirmDialog', () => {
  it('renderiza titulo y mensaje', () => {
    render(
      <ConfirmDialog
        title="Eliminar producto"
        message="¿Desea continuar?"
        onConfirm={() => undefined}
        onCancel={() => undefined}
      />,
    )

    expect(screen.getByText('Eliminar producto')).toBeInTheDocument()
    expect(screen.getByText('¿Desea continuar?')).toBeInTheDocument()
  })

  it('llama onCancel al pulsar Cancelar y onConfirm al confirmar', () => {
    const onConfirm = vi.fn()
    const onCancel = vi.fn()

    render(
      <ConfirmDialog
        title="Anular la venta"
        message="¿Desea continuar?"
        confirmLabel="Anular venta"
        onConfirm={onConfirm}
        onCancel={onCancel}
      />,
    )

    fireEvent.click(screen.getByText('Cancelar'))
    expect(onCancel).toHaveBeenCalledTimes(1)

    fireEvent.click(screen.getByRole('button', { name: 'Anular venta' }))
    expect(onConfirm).toHaveBeenCalledTimes(1)
  })
})
