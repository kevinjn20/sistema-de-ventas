import { useState } from 'react'
import { productoService, type Producto, type ProductoRequest } from '../services/productoService'
import { useCrud } from '../hooks/useCrud'
import ProductoForm from '../components/ProductoForm'
import ConfirmDialog from '../components/ConfirmDialog'

export default function ProductosPage() {
  const { items: productos, loading, error, crear, actualizar, eliminar } = useCrud<Producto, ProductoRequest>(
    productoService,
    'productos',
  )
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Producto | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const handleSave = async (data: ProductoRequest) => {
    if (editing) {
      await actualizar(editing.id, data)
    } else {
      await crear(data)
    }
    setShowForm(false)
    setEditing(null)
  }

  const handleEdit = (producto: Producto) => {
    setEditing(producto)
    setShowForm(true)
  }

  const handleConfirmDelete = async () => {
    if (deletingId !== null) {
      await eliminar(deletingId)
      setDeletingId(null)
    }
  }

  if (loading) {
    return <div className="flex justify-center py-8 text-gray-500">Cargando...</div>
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-800">Productos</h1>
        <button
          onClick={() => { setEditing(null); setShowForm(true) }}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          + Nuevo Producto
        </button>
      </div>

      {error && (
        <div className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
            <h2 className="mb-4 text-lg font-semibold">
              {editing ? 'Editar Producto' : 'Nuevo Producto'}
            </h2>
            <ProductoForm
              initial={editing ? { nombre: editing.nombre, descripcion: editing.descripcion ?? undefined, precio: editing.precio, stock: editing.stock } : undefined}
              onSave={handleSave}
              onCancel={() => { setShowForm(false); setEditing(null) }}
            />
          </div>
        </div>
      )}

      {deletingId !== null && (
        <ConfirmDialog
          title="Eliminar producto"
          message="El producto se desactivará y dejará de estar disponible para nuevas ventas. ¿Desea continuar?"
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeletingId(null)}
        />
      )}

      <div className="overflow-hidden rounded-lg border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Nombre</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Precio</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Stock</th>
              <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {productos.map((p) => (
              <tr key={p.id} className="hover:bg-gray-50">
                <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">{p.nombre}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-700">${Number(p.precio).toFixed(2)}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-700">{p.stock}</td>
                <td className="whitespace-nowrap px-6 py-4 text-right text-sm">
                  <button
                    onClick={() => handleEdit(p)}
                    className="mr-3 text-blue-600 hover:text-blue-800"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => setDeletingId(p.id)}
                    className="text-red-600 hover:text-red-800"
                  >
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
            {productos.length === 0 && (
              <tr>
                <td colSpan={4} className="px-6 py-8 text-center text-sm text-gray-500">
                  No hay productos registrados
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
