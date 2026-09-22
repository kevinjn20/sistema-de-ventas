import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { authService, getStoredUser, type SessionUser } from '../services/authService'
import { clearToken, getToken } from '../services/api'
import { getErrorMessage } from '../services/api'

interface AuthContextValue {
  user: SessionUser | null
  isAdmin: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  isAdmin: false,
  login: async () => undefined,
  logout: () => undefined,
})

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(() =>
    getToken() ? getStoredUser() : null,
  )
  const navigate = useNavigate()

  const logout = useCallback(() => {
    authService.logout()
    setUser(null)
    navigate('/login', { replace: true })
  }, [navigate])

  useEffect(() => {
    const onUnauthorized = () => {
      clearToken()
      setUser(null)
      navigate('/login', { replace: true })
    }
    window.addEventListener('sv:unauthorized', onUnauthorized)
    return () => window.removeEventListener('sv:unauthorized', onUnauthorized)
  }, [navigate])

  const login = useCallback(
    async (username: string, password: string) => {
      try {
        const session = await authService.login(username, password)
        setUser(session)
        toast.success(`Bienvenido, ${session.username}`)
        navigate('/', { replace: true })
      } catch (err) {
        toast.error(getErrorMessage(err, 'Credenciales inválidas'))
        throw err
      }
    },
    [navigate],
  )

  const value = useMemo<AuthContextValue>(
    () => ({ user, isAdmin: user?.rol === 'ADMIN', login, logout }),
    [user, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  return useContext(AuthContext)
}
