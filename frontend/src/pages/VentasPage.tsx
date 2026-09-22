import { useState, useEffect, useCallback } from 'react'
import { toast } from 'sonner'
import { ventaService, type Venta } from '../services/ventaService'
import { useAuth } from '../context/AuthContext'
import { getErrorMessage } from '../services/api'
import VentaForm from '../components/VentaForm'
import ConfirmDialog from '../components/ConfirmDialog'

export default function VentasPage() {
  const { user, isAdmin } = useAuth()
  const [ventas, setVentas] = useState<Venta[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [selected, setSelected] = useState<Venta | null>(null)
  const [anulandoId, setAnulandoId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const cargarVentas = useCallback(async () => {
    try {
      setLoading(true)
      const data = await ventaService.listar()
      setVentas(data)
      setError(null)
    } catch (err) {
      setError(getErrorMessage(err, 'Error al cargar ventas'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void cargarVentas()
  }, [cargarVentas])

  const handleSave = async (data: { clienteId: number; usuarioId: number; items: { productoId: number; cantidad: number }[] }) => {
    try {
      await ventaService.crear(data)
      toast.success('Venta registrada correctamente')
      setShowForm(false)
      await cargarVentas()
    } catch (err) {
      toast.error(getErrorMessage(err, 'Error al registrar la venta'))
      throw err
    }
  }

  const handleConfirmAnular = async () => {
    if (anulandoId === null) return
    try {
      await ventaService.anular(anulandoId)
      toast.success('Venta anulada y stock revertido')
      setAnulandoId(null)
      setSelected(null)
      await cargarVentas()
    } catch (err) {
      toast.error(getErrorMessage(err, 'Error al anular la venta'))
      throw err
    }
  }

  if (loading) {
    return <div className="flex justify-center py-8 text-gray-500">Cargando...</div>
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Ventas</h1>
        <button
          onClick={() => setShowForm(true)}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          + Nueva Venta
        </button>
      </div>

      {error && (
        <div className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl max-h-[90vh] overflow-y-auto">
            <h2 className="mb-4 text-lg font-semibold">Nueva Venta</h2>
            <VentaForm
              usuarioId={user?.userId ?? 0}
              onSave={handleSave}
              onCancel={() => setShowForm(false)}
            />
          </div>
        </div>
      )}

      {anulandoId !== null && (
        <ConfirmDialog
          title="Anular venta"
          message="La venta se desactivará y el stock de los productos será revertido. ¿Desea continuar?"
          confirmLabel="Anular venta"
          onConfirm={handleConfirmAnular}
          onCancel={() => setAnulandoId(null)}
        />
      )}

      {selected && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl max-h-[90vh] overflow-y-auto">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold">Venta #{selected.id}</h2>
              <button
                onClick={() => setSelected(null)}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>
            <div className="mb-4 grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="font-medium text-gray-500">Cliente:</span>{' '}
                <span className="text-gray-900">{selected.cliente.nombre}</span>
              </div>
              <div>
                <span className="font-medium text-gray-500">Vendedor:</span>{' '}
                <span className="text-gray-900">{selected.usuario.username}</span>
              </div>
              <div>
                <span className="font-medium text-gray-500">Fecha:</span>{' '}
                <span className="text-gray-900">{new Date(selected.fecha).toLocaleString()}</span>
              </div>
              <div>
                <span className="font-medium text-gray-500">Total:</span>{' '}
                <span className="text-lg font-bold text-gray-900">${selected.total.toFixed(2)}</span>
              </div>
            </div>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-gray-500">
                  <th className="py-2 text-left font-medium">Producto</th>
                  <th className="py-2 text-right font-medium">Cant.</th>
                  <th className="py-2 text-right font-medium">Precio Unit.</th>
                  <th className="py-2 text-right font-medium">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {selected.detalles.map((d) => (
                  <tr key={d.id} className="border-b">
                    <td className="py-2 text-gray-900">{d.productoNombre}</td>
                    <td className="py-2 text-right">{d.cantidad}</td>
                    <td className="py-2 text-right">${d.precioUnitario.toFixed(2)}</td>
                    <td className="py-2 text-right font-medium">${d.subtotal.toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {isAdmin && (
              <div className="mt-4 flex justify-end">
                <button
                  onClick={() => setAnulandoId(selected.id)}
                  className="rounded-md bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
                >
                  Anular venta
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      <div className="space-y-3">
        {ventas.length === 0 && (
          <div className="rounded-lg border border-gray-200 p-8 text-center text-sm text-gray-500">
            No hay ventas registradas
          </div>
        )}
        {ventas.map((v) => (
          <div
            key={v.id}
            onClick={() => setSelected(v)}
            className="cursor-pointer rounded-lg border border-gray-200 bg-white p-4 transition-colors hover:bg-gray-50"
          >
            <div className="flex items-center justify-between">
              <div>
                <span className="font-semibold text-gray-900">Venta #{v.id}</span>
                <span className="ml-3 text-sm text-gray-500">
                  {new Date(v.fecha).toLocaleDateString()}
                </span>
              </div>
              <div className="flex items-center gap-3 text-right">
                <span className="text-lg font-bold text-gray-900">${v.total.toFixed(2)}</span>
                {isAdmin && (
                  <button
                    onClick={(e) => { e.stopPropagation(); setAnulandoId(v.id) }}
                    className="text-sm text-red-600 hover:text-red-800"
                  >
                    Anular
                  </button>
                )}
              </div>
            </div>
            <div className="mt-1 text-sm text-gray-500">
              {v.cliente.nombre} — {v.usuario.username} — {v.detalles.length} producto(s)
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
