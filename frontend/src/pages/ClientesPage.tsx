import { useState } from 'react'
import { clienteService, type Cliente, type ClienteRequest } from '../services/clienteService'
import { useCrud } from '../hooks/useCrud'
import ClienteForm from '../components/ClienteForm'
import ConfirmDialog from '../components/ConfirmDialog'

export default function ClientesPage() {
  const { items: clientes, loading, error, crear, actualizar, eliminar } = useCrud<Cliente, ClienteRequest>(
    clienteService,
    'clientes',
  )
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState<Cliente | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const handleSave = async (data: ClienteRequest) => {
    if (editing) {
      await actualizar(editing.id, data)
    } else {
      await crear(data)
    }
    setShowForm(false)
    setEditing(null)
  }

  const handleEdit = (cliente: Cliente) => {
    setEditing(cliente)
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
        <h1 className="text-2xl font-bold text-gray-800">Clientes</h1>
        <button
          onClick={() => { setEditing(null); setShowForm(true) }}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          + Nuevo Cliente
        </button>
      </div>

      {error && (
        <div className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
          <div className="w-full max-w-lg rounded-lg bg-white p-6 shadow-xl">
            <h2 className="mb-4 text-lg font-semibold">
              {editing ? 'Editar Cliente' : 'Nuevo Cliente'}
            </h2>
            <ClienteForm
              initial={editing ? { nombre: editing.nombre, email: editing.email ?? undefined, telefono: editing.telefono ?? undefined, direccion: editing.direccion ?? undefined } : undefined}
              onSave={handleSave}
              onCancel={() => { setShowForm(false); setEditing(null) }}
            />
          </div>
        </div>
      )}

      {deletingId !== null && (
        <ConfirmDialog
          title="Eliminar cliente"
          message="El cliente se desactivará y no aparecerá en nuevas ventas. ¿Desea continuar?"
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeletingId(null)}
        />
      )}

      <div className="overflow-hidden rounded-lg border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Nombre</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Email</th>
              <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Teléfono</th>
              <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 bg-white">
            {clientes.map((c) => (
              <tr key={c.id} className="hover:bg-gray-50">
                <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-gray-900">{c.nombre}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-700">{c.email ?? '-'}</td>
                <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-700">{c.telefono ?? '-'}</td>
                <td className="whitespace-nowrap px-6 py-4 text-right text-sm">
                  <button
                    onClick={() => handleEdit(c)}
                    className="mr-3 text-blue-600 hover:text-blue-800"
                  >
                    Editar
                  </button>
                  <button
                    onClick={() => setDeletingId(c.id)}
                    className="text-red-600 hover:text-red-800"
                  >
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
            {clientes.length === 0 && (
              <tr>
                <td colSpan={4} className="px-6 py-8 text-center text-sm text-gray-500">
                  No hay clientes registrados
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
