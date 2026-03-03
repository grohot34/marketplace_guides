import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'

export default function AuthCallbackPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)

  useEffect(() => {
    const token = searchParams.get('token')
    const username = searchParams.get('username')
    const role = searchParams.get('role')
    const error = searchParams.get('error')

    if (error) {
      toast.error(error || 'Ошибка входа через Google')
      navigate('/login', { replace: true })
      return
    }

    if (token && username && role) {
      setAuth(token, username, role)
      toast.success('Вход через Google выполнен успешно!')
      navigate('/', { replace: true })
    } else {
      navigate('/login', { replace: true })
    }
  }, [searchParams, setAuth, navigate])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mb-4"></div>
        <p className="text-gray-600">Выполняется вход...</p>
      </div>
    </div>
  )
}
