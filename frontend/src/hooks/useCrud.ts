import { useCallback, useEffect, useState } from 'react'
import { toast } from 'sonner'
import { getErrorMessage } from '../services/api'

export interface CrudOperations<T, R> {
  listar: () => Promise<T[]>
  crear: (request: R) => Promise<T>
  actualizar: (id: number, request: R) => Promise<T>
  eliminar: (id: number) => Promise<void>
}

export function useCrud<T extends { id: number }, R>(operations: CrudOperations<T, R>, resourceName: string) {
  const [items, setItems] = useState<T[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const recargar = useCallback(async () => {
    try {
      setLoading(true)
      const data = await operations.listar()
      setItems(data)
      setError(null)
    } catch (err) {
      setError(getErrorMessage(err, `Error al cargar ${resourceName}`))
    } finally {
      setLoading(false)
    }
  }, [operations, resourceName])

  useEffect(() => {
    void recargar()
  }, [recargar])

  const crear = useCallback(
    async (request: R) => {
      try {
        const created = await operations.crear(request)
        toast.success('Creado correctamente')
        await recargar()
        return created
      } catch (err) {
        toast.error(getErrorMessage(err, 'Error al crear'))
        throw err
      }
    },
    [operations, recargar],
  )

  const actualizar = useCallback(
    async (id: number, request: R) => {
      try {
        const updated = await operations.actualizar(id, request)
        toast.success('Actualizado correctamente')
        await recargar()
        return updated
      } catch (err) {
        toast.error(getErrorMessage(err, 'Error al actualizar'))
        throw err
      }
    },
    [operations, recargar],
  )

  const eliminar = useCallback(
    async (id: number) => {
      try {
        await operations.eliminar(id)
        toast.success('Eliminado correctamente')
        await recargar()
      } catch (err) {
        toast.error(getErrorMessage(err, 'Error al eliminar'))
        throw err
      }
    },
    [operations, recargar],
  )

  return { items, loading, error, recargar, crear, actualizar, eliminar }
}
