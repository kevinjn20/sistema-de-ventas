import { useEffect, useState } from 'react'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts'
import { dashboardService, Dashboard } from '../services/dashboardService'

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']

function StatCard({ label, value, accent }: { label: string; value: string | number; accent: string }) {
  return (
    <div className={`rounded-lg border-l-4 ${accent} bg-white p-4 shadow-sm`}>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-2xl font-bold text-gray-800">{value}</p>
    </div>
  )
}

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')

  const cargar = async (f?: string, t?: string) => {
    try {
      setLoading(true)
      const data = await dashboardService.obtener(
        f || undefined,
        t || undefined,
      )
      setDashboard(data)
      setError(null)
    } catch {
      setError('Error al cargar el dashboard')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void cargar()
  }, [])

  if (loading) {
    return <div className="mx-auto max-w-5xl px-4 py-8 text-center text-gray-500">Cargando dashboard...</div>
  }

  if (error || !dashboard) {
    return <div className="mx-auto max-w-5xl px-4 py-8 text-center text-red-500">{error}</div>
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8 space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <h1 className="text-2xl font-bold text-gray-800">Dashboard</h1>
        <form
          className="flex items-end gap-2 text-sm"
          onSubmit={(e) => { e.preventDefault(); void cargar(from, to) }}
        >
          <label className="flex flex-col text-gray-500">
            Desde
            <input
              type="date"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              className="rounded-md border border-gray-300 px-2 py-1 text-gray-800"
            />
          </label>
          <label className="flex flex-col text-gray-500">
            Hasta
            <input
              type="date"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              className="rounded-md border border-gray-300 px-2 py-1 text-gray-800"
            />
          </label>
          <button
            type="submit"
            className="rounded-md bg-blue-600 px-3 py-1.5 font-medium text-white hover:bg-blue-700"
          >
            Filtrar
          </button>
          {(from || to) && (
            <button
              type="button"
              onClick={() => { setFrom(''); setTo(''); void cargar() }}
              className="rounded-md border border-gray-300 px-3 py-1.5 text-gray-600 hover:bg-gray-50"
            >
              Limpiar
            </button>
          )}
        </form>
      </div>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard label="Ventas Totales" value={dashboard.totalVentas} accent="border-blue-500" />
        <StatCard label="Ingresos Totales" value={`$${Number(dashboard.ingresosTotales).toLocaleString('en-US')}`} accent="border-green-500" />
        <StatCard label="Clientes Activos" value={dashboard.clientesActivos} accent="border-yellow-500" />
        <StatCard label="Usuarios Activos" value={dashboard.usuariosActivos} accent="border-purple-500" />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="rounded-lg bg-white p-4 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold text-gray-700">Ventas por Día</h2>
          {dashboard.ventasPorDia.length === 0 ? (
            <p className="text-center text-gray-400">Sin datos</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={dashboard.ventasPorDia}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="fecha" tick={{ fontSize: 12 }} />
                <YAxis />
                <Tooltip />
                <Bar dataKey="total" fill="#3b82f6" radius={[4, 4, 0, 0]} name="Total $" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="rounded-lg bg-white p-4 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold text-gray-700">Top Productos</h2>
          {dashboard.topProductos.length === 0 ? (
            <p className="text-center text-gray-400">Sin datos</p>
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={dashboard.topProductos}
                  dataKey="cantidadVendida"
                  nameKey="nombre"
                  cx="50%"
                  cy="50%"
                  outerRadius={90}
                  label={({ nombre, percent }) => `${nombre} ${(percent * 100).toFixed(0)}%`}
                >
                  {dashboard.topProductos.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="rounded-lg bg-white p-4 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-gray-700">Productos con Stock Bajo</h2>
        {dashboard.productosBajoStock.length === 0 ? (
          <p className="text-center text-gray-400">No hay productos con stock bajo</p>
        ) : (
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b text-gray-500">
                <th className="pb-2 font-medium">Producto</th>
                <th className="pb-2 font-medium">Stock</th>
              </tr>
            </thead>
            <tbody>
              {dashboard.productosBajoStock.map((p) => (
                <tr key={p.id} className="border-b last:border-0">
                  <td className="py-2 text-gray-800">{p.nombre}</td>
                  <td className="py-2">
                    <span className="rounded bg-red-100 px-2 py-0.5 text-xs font-semibold text-red-600">
                      {p.stock}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
