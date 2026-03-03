import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '../api/services'
import type { Review } from '../api/tours'
import {
  Users,
  BarChart3,
  Trash2,
  TrendingUp,
  Plus,
  X as XIcon,
  Search,
  Filter,
  MapPin,
  CalendarCheck,
  MessageSquare,
  CheckCircle,
  XCircle,
  Reply,
} from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'

type ReviewStatusFilter = 'PENDING' | 'APPROVED' | 'REJECTED' | ''

export default function AdminPage() {
  const [activeTab, setActiveTab] = useState<'stats' | 'users' | 'reviews'>('stats')
  const [showCreateUser, setShowCreateUser] = useState(false)
  const [userRoleFilter, setUserRoleFilter] = useState<string>('ALL')
  const [userStatusFilter, setUserStatusFilter] = useState<string>('ALL')
  const [userSearch, setUserSearch] = useState('')
  const [reviewStatusFilter, setReviewStatusFilter] = useState<ReviewStatusFilter>('PENDING')
  const [responseModal, setResponseModal] = useState<{ review: Review; response: string } | null>(null)
  const queryClient = useQueryClient()

  const { data: stats } = useQuery({
    queryKey: ['admin', 'stats'],
    queryFn: async () => {
      const response = await adminApi.getStats()
      return response.data
    },
  })

  const { data: users } = useQuery({
    queryKey: ['admin', 'users'],
    queryFn: async () => {
      const response = await adminApi.getAllUsers()
      return response.data
    },
    enabled: activeTab === 'users',
  })

  const updateUserStatusMutation = useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) =>
      adminApi.updateUserStatus(id, active),
    onSuccess: () => {
      toast.success('Статус пользователя обновлён')
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stats'] })
    },
    onError: () => toast.error('Ошибка при обновлении статуса'),
  })

  const updateUserRoleMutation = useMutation({
    mutationFn: ({ id, role }: { id: number; role: string }) =>
      adminApi.updateUserRole(id, role),
    onSuccess: () => {
      toast.success('Роль пользователя обновлена')
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stats'] })
    },
    onError: () => toast.error('Ошибка при обновлении роли'),
  })

  const deleteUserMutation = useMutation({
    mutationFn: adminApi.deleteUser,
    onSuccess: () => {
      toast.success('Пользователь удалён')
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stats'] })
    },
    onError: () => toast.error('Ошибка при удалении пользователя'),
  })

  const createUserMutation = useMutation({
    mutationFn: adminApi.createUser,
    onSuccess: () => {
      toast.success('Пользователь создан')
      setShowCreateUser(false)
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] })
      queryClient.invalidateQueries({ queryKey: ['admin', 'stats'] })
    },
    onError: () => toast.error('Ошибка при создании пользователя'),
  })

  const { data: reviews } = useQuery({
    queryKey: ['admin', 'reviews', reviewStatusFilter],
    queryFn: async () => {
      const res = await adminApi.getReviews(
        reviewStatusFilter === '' ? undefined : (reviewStatusFilter as 'PENDING' | 'APPROVED' | 'REJECTED')
      )
      return res.data || []
    },
    enabled: activeTab === 'reviews',
  })

  const setReviewStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: 'APPROVED' | 'REJECTED' }) =>
      adminApi.setReviewStatus(id, status),
    onSuccess: () => {
      toast.success('Статус отзыва обновлён')
      queryClient.invalidateQueries({ queryKey: ['admin', 'reviews'] })
    },
    onError: () => toast.error('Ошибка при обновлении статуса'),
  })

  const setReviewResponseMutation = useMutation({
    mutationFn: ({ id, response }: { id: number; response: string }) =>
      adminApi.setReviewResponse(id, response),
    onSuccess: () => {
      toast.success('Ответ сохранён')
      setResponseModal(null)
      queryClient.invalidateQueries({ queryKey: ['admin', 'reviews'] })
    },
    onError: () => toast.error('Ошибка при сохранении ответа'),
  })

  const filteredUsers =
    users?.filter((user) => {
      if (userRoleFilter !== 'ALL' && user.role !== userRoleFilter) return false
      if (userStatusFilter !== 'ALL') {
        const isActive = user.active !== false
        if (userStatusFilter === 'ACTIVE' && !isActive) return false
        if (userStatusFilter === 'INACTIVE' && isActive) return false
      }
      if (userSearch) {
        const search = userSearch.toLowerCase()
        const fullName = `${user.firstName} ${user.lastName}`.toLowerCase()
        if (
          !fullName.includes(search) &&
          !user.email.toLowerCase().includes(search) &&
          !user.username.toLowerCase().includes(search)
        ) {
          return false
        }
      }
      return true
    }) || []

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Панель администратора</h1>

      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('stats')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'stats'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <BarChart3 className="inline mr-2" size={18} />
            Статистика
          </button>
          <button
            onClick={() => setActiveTab('users')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'users'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <Users className="inline mr-2" size={18} />
            Пользователи
          </button>
          <button
            onClick={() => setActiveTab('reviews')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'reviews'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <MessageSquare className="inline mr-2" size={18} />
            Модерация отзывов
          </button>
        </nav>
      </div>

      {activeTab === 'stats' && stats && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <Users className="text-blue-500" size={24} />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Пользователей</p>
                <p className="text-2xl font-bold text-gray-900">{stats.totalUsers}</p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <MapPin className="text-green-500" size={24} />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Экскурсий</p>
                <p className="text-2xl font-bold text-gray-900">
                  {stats.totalTours ?? stats.totalServices ?? 0}
                </p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <CalendarCheck className="text-purple-500" size={24} />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Бронирований</p>
                <p className="text-2xl font-bold text-gray-900">
                  {stats.totalBookings ?? stats.totalOrders ?? 0}
                </p>
              </div>
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center">
              <TrendingUp className="text-amber-500" size={24} />
              <div className="ml-4">
                <p className="text-sm font-medium text-gray-600">Выручка (завершённые)</p>
                <p className="text-2xl font-bold text-gray-900">
                  {Number(stats.totalRevenue ?? 0).toLocaleString('ru-RU')} BYN
                </p>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'reviews' && (
        <div className="space-y-4">
          <div className="flex items-center gap-4">
            <Filter className="text-gray-500" size={20} />
            <select
              value={reviewStatusFilter}
              onChange={(e) => setReviewStatusFilter(e.target.value as ReviewStatusFilter)}
              className="border border-gray-300 rounded-md px-3 py-2"
            >
              <option value="">Все отзывы</option>
              <option value="PENDING">Ожидают модерации</option>
              <option value="APPROVED">Одобрены</option>
              <option value="REJECTED">Отклонены</option>
            </select>
          </div>
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Автор</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Гид</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Тур</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Оценка</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Комментарий</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Статус</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Действия</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {(reviews || []).map((rev) => (
                  <tr key={rev.id}>
                    <td className="px-4 py-3 text-sm text-gray-900">{rev.customerName}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{rev.guideName}</td>
                    <td className="px-4 py-3 text-sm text-gray-600">{rev.tourTitle}</td>
                    <td className="px-4 py-3 text-sm">{rev.rating} ★</td>
                    <td className="px-4 py-3 text-sm text-gray-600 max-w-xs truncate">{rev.comment || '—'}</td>
                    <td className="px-4 py-3">
                      <span
                        className={`px-2 py-1 rounded text-xs font-medium ${
                          rev.status === 'APPROVED'
                            ? 'bg-green-100 text-green-800'
                            : rev.status === 'REJECTED'
                            ? 'bg-red-100 text-red-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {rev.status === 'PENDING' ? 'На модерации' : rev.status === 'APPROVED' ? 'Одобрен' : 'Отклонён'}
                      </span>
                    </td>
                    <td className="px-4 py-3 flex flex-wrap gap-1">
                      {rev.status === 'PENDING' && (
                        <>
                          <button
                            onClick={() => setReviewStatusMutation.mutate({ id: rev.id, status: 'APPROVED' })}
                            disabled={setReviewStatusMutation.isPending}
                            className="inline-flex items-center px-2 py-1 text-xs font-medium text-green-700 bg-green-100 rounded hover:bg-green-200"
                          >
                            <CheckCircle size={14} className="mr-0.5" />
                            Одобрить
                          </button>
                          <button
                            onClick={() => setReviewStatusMutation.mutate({ id: rev.id, status: 'REJECTED' })}
                            disabled={setReviewStatusMutation.isPending}
                            className="inline-flex items-center px-2 py-1 text-xs font-medium text-red-700 bg-red-100 rounded hover:bg-red-200"
                          >
                            <XCircle size={14} className="mr-0.5" />
                            Отклонить
                          </button>
                        </>
                      )}
                      <button
                        onClick={() =>
                          setResponseModal({ review: rev, response: rev.response || '' })
                        }
                        className="inline-flex items-center px-2 py-1 text-xs font-medium text-primary-700 bg-primary-100 rounded hover:bg-primary-200"
                      >
                        <Reply size={14} className="mr-0.5" />
                        Ответить
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {(!reviews || reviews.length === 0) && (
              <div className="px-4 py-8 text-center text-gray-500">Нет отзывов</div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'users' && (
        <div className="space-y-4">
          <div className="flex justify-between items-center gap-4">
            <div className="flex-1 flex items-center gap-4">
              <div className="relative flex-1 max-w-md">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                <input
                  type="text"
                  placeholder="Поиск пользователей..."
                  value={userSearch}
                  onChange={(e) => setUserSearch(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border rounded-md"
                />
              </div>
              <div className="flex items-center gap-2">
                <Filter className="text-gray-500" size={20} />
                <select
                  value={userRoleFilter}
                  onChange={(e) => setUserRoleFilter(e.target.value)}
                  className="border border-gray-300 rounded-md px-3 py-2"
                >
                  <option value="ALL">Все роли</option>
                  <option value="CUSTOMER">Турист</option>
                  <option value="GUIDE">Гид</option>
                  <option value="ADMIN">Администратор</option>
                </select>
                <select
                  value={userStatusFilter}
                  onChange={(e) => setUserStatusFilter(e.target.value)}
                  className="border border-gray-300 rounded-md px-3 py-2"
                >
                  <option value="ALL">Все статусы</option>
                  <option value="ACTIVE">Активные</option>
                  <option value="INACTIVE">Неактивные</option>
                </select>
              </div>
            </div>
            <button
              onClick={() => setShowCreateUser(true)}
              className="flex items-center px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
            >
              <Plus className="mr-2" size={18} />
              Создать пользователя
            </button>
          </div>
          <div className="bg-white rounded-lg shadow overflow-hidden">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Имя</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Email</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Роль</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Статус</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Действия</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredUsers.map((user) => (
                  <tr key={user.id ?? `user-${user.email}`}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.id ?? '-'}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {user.firstName} {user.lastName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{user.email}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <select
                        value={user.role}
                        onChange={(e) =>
                          user.id !== undefined &&
                          updateUserRoleMutation.mutate({ id: user.id, role: e.target.value })
                        }
                        className="border border-gray-300 rounded px-2 py-1 text-gray-900 bg-white"
                      >
                        <option value="CUSTOMER">Турист</option>
                        <option value="GUIDE">Гид</option>
                        <option value="ADMIN">Администратор</option>
                      </select>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {user.id !== undefined && (
                        <button
                          onClick={() =>
                            updateUserStatusMutation.mutate({ id: user.id!, active: !user.active })
                          }
                          className={`px-3 py-1 rounded-full text-xs font-medium ${
                            user.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                          }`}
                        >
                          {user.active ? 'Активен' : 'Неактивен'}
                        </button>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      {user.id !== undefined && (
                        <button
                          onClick={() => {
                            if (confirm('Удалить этого пользователя?')) {
                              deleteUserMutation.mutate(user.id!)
                            }
                          }}
                          className="text-red-600 hover:text-red-900"
                        >
                          <Trash2 size={18} />
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showCreateUser && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold">Создать пользователя</h2>
              <button onClick={() => setShowCreateUser(false)}>
                <XIcon size={24} />
              </button>
            </div>
            <form
              onSubmit={(e) => {
                e.preventDefault()
                const formData = new FormData(e.target as HTMLFormElement)
                createUserMutation.mutate({
                  username: formData.get('username') as string,
                  email: formData.get('email') as string,
                  password: formData.get('password') as string,
                  firstName: formData.get('firstName') as string,
                  lastName: formData.get('lastName') as string,
                  phone: (formData.get('phone') as string) || undefined,
                  address: (formData.get('address') as string) || undefined,
                  role: formData.get('role') as string,
                  active: true,
                })
              }}
              className="space-y-4"
            >
              <input
                name="username"
                placeholder="Логин"
                required
                className="w-full border rounded px-3 py-2"
              />
              <input
                name="email"
                type="email"
                placeholder="Email"
                required
                className="w-full border rounded px-3 py-2"
              />
              <input
                name="password"
                type="password"
                placeholder="Пароль"
                required
                className="w-full border rounded px-3 py-2"
              />
              <input name="firstName" placeholder="Имя" required className="w-full border rounded px-3 py-2" />
              <input name="lastName" placeholder="Фамилия" required className="w-full border rounded px-3 py-2" />
              <input name="phone" placeholder="Телефон" className="w-full border rounded px-3 py-2" />
              <input name="address" placeholder="Адрес" className="w-full border rounded px-3 py-2" />
              <select name="role" required className="w-full border rounded px-3 py-2">
                <option value="CUSTOMER">Турист</option>
                <option value="GUIDE">Гид</option>
                <option value="ADMIN">Администратор</option>
              </select>
              <div className="flex gap-2">
                <button
                  type="submit"
                  className="flex-1 bg-primary-600 text-white px-4 py-2 rounded hover:bg-primary-700"
                >
                  Создать
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateUser(false)}
                  className="flex-1 bg-gray-200 text-gray-800 px-4 py-2 rounded hover:bg-gray-300"
                >
                  Отмена
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {responseModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 max-w-lg w-full">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold">Ответ на отзыв</h2>
              <button onClick={() => setResponseModal(null)}>
                <XIcon size={24} />
              </button>
            </div>
            <p className="text-sm text-gray-600 mb-2">
              Отзыв от {responseModal.review.customerName}: «{responseModal.review.comment}»
            </p>
            <textarea
              value={responseModal.response}
              onChange={(e) =>
                setResponseModal((prev) => (prev ? { ...prev, response: e.target.value } : null))
              }
              className="w-full border rounded px-3 py-2 mb-4"
              rows={4}
              placeholder="Текст ответа (будет показан под отзывом)"
            />
            <div className="flex gap-2">
              <button
                onClick={() => {
                  setReviewResponseMutation.mutate({
                    id: responseModal.review.id,
                    response: responseModal.response,
                  })
                }}
                disabled={setReviewResponseMutation.isPending}
                className="flex-1 bg-primary-600 text-white py-2 rounded-md hover:bg-primary-700 disabled:opacity-50"
              >
                {setReviewResponseMutation.isPending ? 'Сохранение...' : 'Сохранить ответ'}
              </button>
              <button
                onClick={() => setResponseModal(null)}
                className="px-4 py-2 border rounded-md hover:bg-gray-50"
              >
                Отмена
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
