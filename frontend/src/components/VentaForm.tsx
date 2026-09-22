import { useState, useEffect } from 'react'
import type { VentaRequest } from '../services/ventaService'
import { clienteService, type Cliente } from '../services/clienteService'
import { productoService, type Producto } from '../services/productoService'

interface Props {
  usuarioId: number
  onSave: (data: VentaRequest) => Promise<void>
  onCancel: () => void
}

interface ItemEntry {
  productoId: number
  cantidad: number
}

export default function VentaForm({ usuarioId, onSave, onCancel }: Props) {
  const [clientes, setClientes] = useState<Cliente[]>([])
  const [productos, setProductos] = useState<Producto[]>([])
  const [clienteId, setClienteId] = useState('')
  const [items, setItems] = useState<ItemEntry[]>([])
  const [selectedProducto, setSelectedProducto] = useState('')
  const [selectedCantidad, setSelectedCantidad] = useState(1)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    clienteService.listar().then(setClientes)
    productoService.listar().then(setProductos)
  }, [])

  const agregarItem = () => {
    if (!selectedProducto) return
    const id = Number(selectedProducto)
    const existente = items.find((i) => i.productoId === id)
    if (existente) {
      existente.cantidad += selectedCantidad
      setItems([...items])
    } else {
      setItems([...items, { productoId: id, cantidad: selectedCantidad }])
    }
    setSelectedProducto('')
    setSelectedCantidad(1)
  }

  const quitarItem = (productoId: number) => {
    setItems(items.filter((i) => i.productoId !== productoId))
  }

  const calcularSubtotal = (productoId: number, cantidad: number) => {
    const p = productos.find((pr) => pr.id === productoId)
    return p ? p.precio * cantidad : 0
  }

  const calcularTotal = () => {
    return items.reduce((sum, i) => sum + calcularSubtotal(i.productoId, i.cantidad), 0)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (items.length === 0) return
    setLoading(true)
    try {
      await onSave({
        clienteId: Number(clienteId),
        usuarioId,
        items: items.map((i) => ({ productoId: i.productoId, cantidad: i.cantidad })),
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700">Cliente</label>
        <select
          value={clienteId}
          onChange={(e) => setClienteId(e.target.value)}
          required
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        >
          <option value="">Seleccionar cliente...</option>
          {clientes.map((c) => (
            <option key={c.id} value={c.id}>{c.nombre}</option>
          ))}
        </select>
      </div>

      <div className="border rounded-lg p-4 space-y-3">
        <h3 className="text-sm font-semibold text-gray-700">Productos</h3>
        <div className="flex gap-3">
          <select
            value={selectedProducto}
            onChange={(e) => setSelectedProducto(e.target.value)}
            className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none"
          >
            <option value="">Seleccionar producto...</option>
            {productos.filter((p) => p.stock > 0).map((p) => (
              <option key={p.id} value={p.id}>
                {p.nombre} — ${p.precio} (stock: {p.stock})
              </option>
            ))}
          </select>
          <input
            type="number"
            min={1}
            value={selectedCantidad}
            onChange={(e) => setSelectedCantidad(Math.max(1, Number(e.target.value)))}
            className="w-20 rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-500 focus:outline-none"
          />
          <button
            type="button"
            onClick={agregarItem}
            disabled={!selectedProducto}
            className="rounded-md bg-green-600 px-3 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-50"
          >
            Agregar
          </button>
        </div>

        {items.length > 0 && (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b text-gray-500">
                <th className="py-1 text-left font-medium">Producto</th>
                <th className="py-1 text-right font-medium">Cant.</th>
                <th className="py-1 text-right font-medium">Precio</th>
                <th className="py-1 text-right font-medium">Subtotal</th>
                <th className="py-1"></th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => {
                const p = productos.find((pr) => pr.id === item.productoId)
                if (!p) return null
                return (
                  <tr key={item.productoId} className="border-b">
                    <td className="py-1 text-gray-900">{p.nombre}</td>
                    <td className="py-1 text-right">{item.cantidad}</td>
                    <td className="py-1 text-right">${p.precio.toFixed(2)}</td>
                    <td className="py-1 text-right font-medium">${(p.precio * item.cantidad).toFixed(2)}</td>
                    <td className="py-1 text-right">
                      <button
                        type="button"
                        onClick={() => quitarItem(item.productoId)}
                        className="text-red-600 hover:text-red-800 text-xs"
                      >
                        Quitar
                      </button>
                    </td>
                  </tr>
                )
              })}
            </tbody>
            <tfoot>
              <tr className="font-semibold text-gray-900">
                <td colSpan={3} className="py-2 text-right">Total:</td>
                <td className="py-2 text-right">${calcularTotal().toFixed(2)}</td>
                <td></td>
              </tr>
            </tfoot>
          </table>
        )}
      </div>

      <div className="flex justify-end gap-3 pt-2">
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Cancelar
        </button>
        <button
          type="submit"
          disabled={loading || !clienteId || items.length === 0}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
        >
          {loading ? 'Procesando...' : 'Crear Venta'}
        </button>
      </div>
    </form>
  )
}
