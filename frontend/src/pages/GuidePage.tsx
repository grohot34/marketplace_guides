import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { tourApi, bookingApi, reviewApi, guideApi, Tour, Booking } from '../api/tours'
import { userApi, categoryApi } from '../api/services'
import { Plus, Edit, Trash2, Calendar, Users, Search, Filter, Star, MessageSquare, BarChart3, TrendingUp, DollarSign, X } from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'

const statusLabels: Record<string, string> = {
  PENDING: 'Ожидает подтверждения',
  CONFIRMED: 'Подтверждено',
  IN_PROGRESS: 'В процессе',
  COMPLETED: 'Завершено',
  CANCELLED: 'Отменено',
}

const statusColors: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  CONFIRMED: 'bg-blue-100 text-blue-800',
  IN_PROGRESS: 'bg-purple-100 text-purple-800',
  COMPLETED: 'bg-green-100 text-green-800',
  CANCELLED: 'bg-red-100 text-red-800',
}

export default function GuidePage() {
  const [activeTab, setActiveTab] = useState<'stats' | 'tours' | 'bookings' | 'reviews'>('stats')
  const [showCreateTour, setShowCreateTour] = useState(false)
  const [editingTour, setEditingTour] = useState<Tour | null>(null)
  const [tourToDelete, setTourToDelete] = useState<Tour | null>(null)
  const [bookingStatusFilter, setBookingStatusFilter] = useState<string>('ALL')
  const [bookingSearch, setBookingSearch] = useState('')
  const queryClient = useQueryClient()

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const response = await categoryApi.getAll()
      return response.data
    },
    enabled: showCreateTour || !!editingTour,
  })

  const { data: tours, isLoading: toursLoading } = useQuery({
    queryKey: ['tours', 'my-tours'],
    queryFn: async () => {
      const response = await tourApi.getMyTours()
      return response.data
    },
  })

  const { data: bookings, isLoading: bookingsLoading } = useQuery({
    queryKey: ['bookings', 'my-guide-bookings'],
    queryFn: async () => {
      const response = await bookingApi.getMyGuideBookings()
      return response.data
    },
  })

  const { data: currentUser } = useQuery({
    queryKey: ['user', 'me'],
    queryFn: async () => {
      const response = await userApi.getMe()
      return response.data
    },
    enabled: activeTab === 'reviews' || activeTab === 'stats',
  })

  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['guide', 'stats'],
    queryFn: async () => {
      const response = await guideApi.getMyStats()
      return response.data
    },
    enabled: activeTab === 'stats',
  })

  const { data: reviews, isLoading: reviewsLoading } = useQuery({
    queryKey: ['reviews', 'guide', currentUser?.id],
    queryFn: async () => {
      const userId = currentUser?.id
      if (userId == null) throw new Error('User id required')
      const response = await reviewApi.getByGuide(userId)
      return response.data
    },
    enabled: !!currentUser?.id && activeTab === 'reviews',
  })

  const deleteTourMutation = useMutation({
    mutationFn: tourApi.delete,
    onSuccess: () => {
      toast.success('Экскурсия удалена')
      setTourToDelete(null)
      queryClient.invalidateQueries({ queryKey: ['tours'] })
    },
    onError: () => {
      toast.error('Ошибка при удалении экскурсии')
      setTourToDelete(null)
    },
  })

  const createTourMutation = useMutation({
    mutationFn: tourApi.create,
    onSuccess: () => {
      toast.success('Экскурсия создана')
      setShowCreateTour(false)
      queryClient.invalidateQueries({ queryKey: ['tours'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Ошибка при создании экскурсии')
    },
  })

  const updateTourMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Parameters<typeof tourApi.update>[1] }) =>
      tourApi.update(id, data),
    onSuccess: () => {
      toast.success('Экскурсия обновлена')
      setEditingTour(null)
      queryClient.invalidateQueries({ queryKey: ['tours'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Ошибка при обновлении экскурсии')
    },
  })

  const updateBookingStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: Booking['status'] }) =>
      bookingApi.updateStatus(id, status),
    onSuccess: () => {
      toast.success('Статус бронирования обновлен')
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
    },
    onError: () => {
      toast.error('Ошибка при обновлении статуса')
    },
  })

  const filteredBookings = bookings?.filter((booking) => {
    if (bookingStatusFilter !== 'ALL' && booking.status !== bookingStatusFilter) return false
    if (bookingSearch) {
      const search = bookingSearch.toLowerCase()
      if (!booking.tourTitle?.toLowerCase().includes(search) &&
          !booking.customerName?.toLowerCase().includes(search)) {
        return false
      }
    }
    return true
  }) || []

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Панель гида</h1>

      <div className="mb-6 border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('stats')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'stats'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Статистика
          </button>
          <button
            onClick={() => setActiveTab('tours')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'tours'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Мои экскурсии
          </button>
          <button
            onClick={() => setActiveTab('bookings')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'bookings'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Бронирования
          </button>
          <button
            onClick={() => setActiveTab('reviews')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'reviews'
                ? 'border-primary-500 text-primary-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Отзывы
          </button>
        </nav>
      </div>

      {activeTab === 'stats' && (
        <div>
          <h2 className="text-2xl font-semibold mb-6 flex items-center">
            <BarChart3 className="mr-2" size={24} />
            Статистика
          </h2>
          {statsLoading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
          ) : stats ? (
            <div className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <div className="bg-white rounded-lg shadow p-6 border-l-4 border-primary-500">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-500">Экскурсий</p>
                      <p className="text-2xl font-bold text-gray-900">{stats.totalTours}</p>
                    </div>
                    <BarChart3 className="text-primary-500" size={32} />
                  </div>
                </div>
                <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-500">Бронирований</p>
                      <p className="text-2xl font-bold text-gray-900">{stats.totalBookings}</p>
                    </div>
                    <Calendar className="text-blue-500" size={32} />
                  </div>
                </div>
                <div className="bg-white rounded-lg shadow p-6 border-l-4 border-green-500">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-500">Завершено</p>
                      <p className="text-2xl font-bold text-gray-900">{stats.completedBookings}</p>
                    </div>
                    <TrendingUp className="text-green-500" size={32} />
                  </div>
                </div>
                <div className="bg-white rounded-lg shadow p-6 border-l-4 border-amber-500">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm text-gray-500">Выручка</p>
                      <p className="text-2xl font-bold text-gray-900">
                        {Number(stats.totalRevenue || 0).toLocaleString('ru-RU')} BYN
                      </p>
                    </div>
                    <DollarSign className="text-amber-500" size={32} />
                  </div>
                </div>
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="bg-white rounded-lg shadow p-6">
                  <h3 className="font-semibold mb-4">По статусам</h3>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-gray-600">Ожидают подтверждения</span>
                      <span className="font-medium">{stats.pendingBookings}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Подтверждено</span>
                      <span className="font-medium">{stats.confirmedBookings}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">В процессе</span>
                      <span className="font-medium">{stats.inProgressBookings}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Завершено</span>
                      <span className="font-medium text-green-600">{stats.completedBookings}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Отменено</span>
                      <span className="font-medium text-red-600">{stats.cancelledBookings}</span>
                    </div>
                  </div>
                </div>
                <div className="bg-white rounded-lg shadow p-6">
                  <h3 className="font-semibold mb-4">Отзывы</h3>
                  <div className="flex items-center gap-4">
                    <div className="flex items-center">
                      <Star className="text-yellow-400 fill-yellow-400" size={24} />
                      <span className="ml-2 text-xl font-bold">
                        {stats.averageRating > 0 ? stats.averageRating.toFixed(1) : '—'}
                      </span>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Средний рейтинг</p>
                      <p className="text-gray-700">{stats.totalReviews} отзывов</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ) : null}
        </div>
      )}

      {activeTab === 'tours' && (
        <div>
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-semibold">Мои экскурсии</h2>
            <button
              onClick={() => setShowCreateTour(true)}
              className="flex items-center px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
            >
              <Plus className="mr-2" size={18} />
              Создать экскурсию
            </button>
          </div>

          {toursLoading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
          ) : tours && tours.length > 0 ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {tours.map((tour) => (
                <div key={tour.id} className="bg-white rounded-lg shadow-md overflow-hidden">
                  {tour.imageUrl && (
                    <img
                      src={tour.imageUrl}
                      alt={tour.title}
                      className="w-full h-48 object-cover"
                      loading="lazy"
                      decoding="async"
                      onError={(e) => {
                        (e.target as HTMLImageElement).style.display = 'none'
                      }}
                    />
                  )}
                  <div className="p-6">
                    <h3 className="text-xl font-semibold text-gray-900 mb-2">{tour.title}</h3>
                    <p className="text-gray-600 mb-4 line-clamp-2">{tour.description}</p>
                    <div className="flex items-center justify-between mb-4">
                      <p className="text-2xl font-bold text-primary-600">
                        {tour.pricePerPerson.toLocaleString('ru-RU')} BYN
                      </p>
                      {tour.averageRating && (
                        <div className="flex items-center">
                          <span className="text-sm font-medium">{tour.averageRating.toFixed(1)}</span>
                        </div>
                      )}
                    </div>
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => setEditingTour(tour)}
                        className="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200"
                      >
                        <Edit className="inline mr-1" size={16} />
                        Редактировать
                      </button>
                      <button
                        type="button"
                        onClick={() => setTourToDelete(tour)}
                        className="px-4 py-2 bg-red-100 text-red-700 rounded-md hover:bg-red-200"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <p className="text-gray-600">У вас пока нет экскурсий</p>
            </div>
          )}
        </div>
      )}

      {activeTab === 'bookings' && (
        <div>
          <h2 className="text-2xl font-semibold mb-6">Бронирования</h2>

          <div className="mb-6 flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
              <input
                type="text"
                placeholder="Поиск бронирований..."
                value={bookingSearch}
                onChange={(e) => setBookingSearch(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border rounded-md"
              />
            </div>
            <div className="flex items-center gap-2">
              <Filter size={20} className="text-gray-400" />
              <select
                value={bookingStatusFilter}
                onChange={(e) => setBookingStatusFilter(e.target.value)}
                className="px-4 py-2 border rounded-md"
              >
                <option value="ALL">Все статусы</option>
                {Object.entries(statusLabels).map(([key, label]) => (
                  <option key={key} value={key}>{label}</option>
                ))}
              </select>
            </div>
          </div>

          {bookingsLoading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
          ) : filteredBookings.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-600">Бронирования не найдены</p>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredBookings.map((booking) => (
                <div key={booking.id} className="bg-white rounded-lg shadow-md p-6">
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <h3 className="text-xl font-semibold text-gray-900 mb-2">
                        {booking.tourTitle}
                      </h3>
                      {booking.customerName && (
                        <p className="text-gray-600">Клиент: {booking.customerName}</p>
                      )}
                    </div>
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${statusColors[booking.status]}`}>
                      {statusLabels[booking.status]}
                    </span>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                    <div className="flex items-center text-gray-600">
                      <Calendar className="mr-2" size={18} />
                      <span>{new Date(booking.tourDateTime).toLocaleString('ru-RU')}</span>
                    </div>
                    <div className="flex items-center text-gray-600">
                      <Users className="mr-2" size={18} />
                      <span>{booking.numberOfParticipants} участник(ов)</span>
                    </div>
                    <div className="flex items-center text-gray-600">
                      <span className="font-semibold">Стоимость:</span>
                      <span className="ml-2 text-lg font-bold text-primary-600">
                        {booking.totalPrice.toLocaleString('ru-RU')} BYN
                      </span>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-2 pt-4 border-t">
                    {booking.status === 'PENDING' && (
                      <>
                        <button
                          onClick={() => updateBookingStatusMutation.mutate({ id: booking.id, status: 'CONFIRMED' })}
                          disabled={updateBookingStatusMutation.isPending}
                          className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                        >
                          Подтвердить
                        </button>
                        <button
                          onClick={() => updateBookingStatusMutation.mutate({ id: booking.id, status: 'CANCELLED' })}
                          disabled={updateBookingStatusMutation.isPending}
                          className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
                        >
                          Отклонить
                        </button>
                      </>
                    )}
                    {booking.status === 'CONFIRMED' && (
                      <>
                        <button
                          onClick={() => updateBookingStatusMutation.mutate({ id: booking.id, status: 'IN_PROGRESS' })}
                          disabled={updateBookingStatusMutation.isPending}
                          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                        >
                          Начать экскурсию
                        </button>
                        <button
                          onClick={() => updateBookingStatusMutation.mutate({ id: booking.id, status: 'CANCELLED' })}
                          disabled={updateBookingStatusMutation.isPending}
                          className="px-4 py-2 bg-red-100 text-red-700 rounded-md hover:bg-red-200 disabled:opacity-50"
                        >
                          Отменить
                        </button>
                      </>
                    )}
                    {booking.status === 'IN_PROGRESS' && (
                      <button
                        onClick={() => updateBookingStatusMutation.mutate({ id: booking.id, status: 'COMPLETED' })}
                        disabled={updateBookingStatusMutation.isPending}
                        className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50"
                      >
                        Завершить экскурсию
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'reviews' && (
        <div>
          <h2 className="text-2xl font-semibold mb-6 flex items-center">
            <MessageSquare className="mr-2" size={24} />
            Отзывы о моих экскурсиях
          </h2>
          {reviewsLoading ? (
            <div className="text-center py-12">
              <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
          ) : reviews && reviews.length > 0 ? (
            <div className="space-y-4">
              {reviews.map((review) => (
                <div key={review.id} className="bg-white rounded-lg shadow-md p-6">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <div className="flex">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <Star
                            key={star}
                            size={18}
                            className={star <= review.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'}
                          />
                        ))}
                      </div>
                      <span className="font-medium">{review.customerName || 'Клиент'}</span>
                    </div>
                    <span className="text-sm text-gray-500">
                      {new Date(review.createdAt).toLocaleDateString('ru-RU')}
                    </span>
                  </div>
                  {review.tourTitle && (
                    <p className="text-sm text-gray-600 mb-2">Экскурсия: {review.tourTitle}</p>
                  )}
                  {review.comment && <p className="text-gray-700">{review.comment}</p>}
                </div>
              ))}
            </div>
          ) : (
            <div className="text-center py-12">
              <p className="text-gray-600">Пока нет отзывов</p>
              <p className="text-sm text-gray-500 mt-2">
                Отзывы появятся после завершённых экскурсий
              </p>
            </div>
          )}
        </div>
      )}

      {tourToDelete && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Удалить экскурсию?</h3>
            <p className="text-gray-600 mb-4">
              Вы уверены, что хотите удалить экскурсию «{tourToDelete.title}»? Это действие нельзя отменить.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                type="button"
                onClick={() => setTourToDelete(null)}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
              >
                Отмена
              </button>
              <button
                type="button"
                onClick={() => deleteTourMutation.mutate(tourToDelete.id)}
                disabled={deleteTourMutation.isPending}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
              >
                {deleteTourMutation.isPending ? 'Удаление...' : 'Удалить'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Модальное окно: Создать экскурсию */}
      {showCreateTour && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-gray-900">Создать экскурсию</h2>
              <button type="button" onClick={() => setShowCreateTour(false)} className="p-1 hover:bg-gray-100 rounded">
                <X size={24} />
              </button>
            </div>
            <form
              onSubmit={(e) => {
                e.preventDefault()
                const form = e.target as HTMLFormElement
                const fd = new FormData(form)
                createTourMutation.mutate({
                  title: fd.get('title') as string,
                  description: (fd.get('description') as string) || undefined,
                  pricePerPerson: Number(fd.get('pricePerPerson')),
                  durationHours: Number(fd.get('durationHours')),
                  maxParticipants: Number(fd.get('maxParticipants')),
                  location: (fd.get('location') as string) || undefined,
                  meetingPoint: (fd.get('meetingPoint') as string) || undefined,
                  itinerary: (fd.get('itinerary') as string) || undefined,
                  imageUrl: (fd.get('imageUrl') as string) || undefined,
                  categoryId: Number(fd.get('categoryId')),
                })
              }}
              className="space-y-4"
            >
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Название *</label>
                <input name="title" required className="w-full px-3 py-2 border rounded-md" placeholder="Название экскурсии" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Описание</label>
                <textarea name="description" rows={3} className="w-full px-3 py-2 border rounded-md" placeholder="Описание" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Цена за человека (BYN) *</label>
                  <input name="pricePerPerson" type="number" step="0.01" min="0" required className="w-full px-3 py-2 border rounded-md" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Длительность (ч) *</label>
                  <input name="durationHours" type="number" step="0.5" min="0.5" required className="w-full px-3 py-2 border rounded-md" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Макс. участников *</label>
                <input name="maxParticipants" type="number" min="1" required className="w-full px-3 py-2 border rounded-md" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Категория *</label>
                <select name="categoryId" required className="w-full px-3 py-2 border rounded-md">
                  <option value="">Выберите категорию</option>
                  {categories?.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Место проведения</label>
                <input name="location" className="w-full px-3 py-2 border rounded-md" placeholder="Город, адрес" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Место встречи</label>
                <input name="meetingPoint" className="w-full px-3 py-2 border rounded-md" placeholder="Где встречаемся" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Программа</label>
                <textarea name="itinerary" rows={2} className="w-full px-3 py-2 border rounded-md" placeholder="Краткая программа" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">URL изображения</label>
                <input name="imageUrl" type="url" className="w-full px-3 py-2 border rounded-md" placeholder="https://..." />
              </div>
              <div className="flex gap-2 pt-2">
                <button type="submit" disabled={createTourMutation.isPending} className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50">
                  {createTourMutation.isPending ? 'Создание...' : 'Создать'}
                </button>
                <button type="button" onClick={() => setShowCreateTour(false)} className="px-4 py-2 border rounded-md hover:bg-gray-50">
                  Отмена
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editingTour && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold text-gray-900">Редактировать экскурсию</h2>
              <button type="button" onClick={() => setEditingTour(null)} className="p-1 hover:bg-gray-100 rounded">
                <X size={24} />
              </button>
            </div>
            <form
              onSubmit={(e) => {
                e.preventDefault()
                const form = e.target as HTMLFormElement
                const fd = new FormData(form)
                updateTourMutation.mutate({
                  id: editingTour.id,
                  data: {
                    title: fd.get('title') as string,
                    description: (fd.get('description') as string) || undefined,
                    pricePerPerson: Number(fd.get('pricePerPerson')),
                    durationHours: Number(fd.get('durationHours')),
                    maxParticipants: Number(fd.get('maxParticipants')),
                    location: (fd.get('location') as string) || undefined,
                    meetingPoint: (fd.get('meetingPoint') as string) || undefined,
                    itinerary: (fd.get('itinerary') as string) || undefined,
                    imageUrl: (fd.get('imageUrl') as string) || undefined,
                    categoryId: Number(fd.get('categoryId')),
                  },
                })
              }}
              className="space-y-4"
            >
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Название *</label>
                <input name="title" defaultValue={editingTour.title} required className="w-full px-3 py-2 border rounded-md" placeholder="Название экскурсии" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Описание</label>
                <textarea name="description" defaultValue={editingTour.description} rows={3} className="w-full px-3 py-2 border rounded-md" placeholder="Описание" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Цена за человека (BYN) *</label>
                  <input name="pricePerPerson" type="number" step="0.01" min="0" defaultValue={editingTour.pricePerPerson} required className="w-full px-3 py-2 border rounded-md" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Длительность (ч) *</label>
                  <input name="durationHours" type="number" step="0.5" min="0.5" defaultValue={editingTour.durationHours} required className="w-full px-3 py-2 border rounded-md" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Макс. участников *</label>
                <input name="maxParticipants" type="number" min="1" defaultValue={editingTour.maxParticipants} required className="w-full px-3 py-2 border rounded-md" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Категория *</label>
                <select name="categoryId" defaultValue={editingTour.categoryId} required className="w-full px-3 py-2 border rounded-md">
                  {categories?.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Место проведения</label>
                <input name="location" defaultValue={editingTour.location} className="w-full px-3 py-2 border rounded-md" placeholder="Город, адрес" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Место встречи</label>
                <input name="meetingPoint" defaultValue={editingTour.meetingPoint} className="w-full px-3 py-2 border rounded-md" placeholder="Где встречаемся" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Программа</label>
                <textarea name="itinerary" defaultValue={editingTour.itinerary} rows={2} className="w-full px-3 py-2 border rounded-md" placeholder="Краткая программа" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">URL изображения</label>
                <input name="imageUrl" type="url" defaultValue={editingTour.imageUrl} className="w-full px-3 py-2 border rounded-md" placeholder="https://..." />
              </div>
              <div className="flex gap-2 pt-2">
                <button type="submit" disabled={updateTourMutation.isPending} className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50">
                  {updateTourMutation.isPending ? 'Сохранение...' : 'Сохранить'}
                </button>
                <button type="button" onClick={() => setEditingTour(null)} className="px-4 py-2 border rounded-md hover:bg-gray-50">
                  Отмена
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
