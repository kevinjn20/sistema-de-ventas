import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const links = [
  { to: '/', label: 'Dashboard' },
  { to: '/ventas', label: 'Ventas' },
  { to: '/productos', label: 'Productos' },
  { to: '/clientes', label: 'Clientes' },
  { to: '/usuarios', label: 'Usuarios' },
]

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  if (!user) {
    return (
      <nav className="bg-white border-b border-gray-200">
        <div className="mx-auto max-w-5xl px-4">
          <div className="flex h-14 items-center">
            <span className="text-lg font-bold text-gray-800">Sistema de Ventas</span>
          </div>
        </div>
      </nav>
    )
  }

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <nav className="bg-white border-b border-gray-200">
      <div className="mx-auto max-w-5xl px-4">
        <div className="flex h-14 items-center justify-between">
          <span className="text-lg font-bold text-gray-800">Sistema de Ventas</span>
          <div className="flex items-center gap-6">
            {links.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors ${
                    isActive
                      ? 'text-blue-600 border-b-2 border-blue-600'
                      : 'text-gray-600 hover:text-blue-600'
                  }`
                }
              >
                {link.label}
              </NavLink>
            ))}
            <span className="text-sm text-gray-500">
              {user.username} ({user.rol})
            </span>
            <button
              onClick={handleLogout}
              className="text-sm font-medium text-red-600 hover:text-red-800"
            >
              Salir
            </button>
          </div>
        </div>
      </div>
    </nav>
  )
}
