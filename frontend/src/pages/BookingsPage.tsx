import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useSearchParams, Link } from 'react-router-dom'
import { bookingApi, reviewApi, paymentApi } from '../api/tours'
import type { Booking, Review } from '../api/tours'
import { Calendar, X, Filter, Search, Users, Star, MessageSquare, Pencil, Trash2, CreditCard } from 'lucide-react'
import { useState, useEffect } from 'react'
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

export default function BookingsPage() {
  const queryClient = useQueryClient()
  const [searchParams, setSearchParams] = useSearchParams()
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [payingBookingId, setPayingBookingId] = useState<number | null>(null)

  useEffect(() => {
    const payment = searchParams.get('payment')
    const sessionId = searchParams.get('session_id')
    if (payment === 'success') {
      const run = async () => {
        if (sessionId) {
          try {
            await paymentApi.confirmSession(sessionId)
          } catch (e) {
            toast.error('Не удалось обновить статус оплаты')
          }
        }
        toast.success('Оплата прошла успешно')
        setSearchParams({}, { replace: true })
        queryClient.invalidateQueries({ queryKey: ['bookings'] })
      }
      run()
    } else if (payment === 'cancelled') {
      toast('Оплата отменена', { icon: 'ℹ️' })
      setSearchParams({}, { replace: true })
    }
  }, [searchParams, setSearchParams, queryClient])
  const [bookingSearch, setBookingSearch] = useState('')
  const [reviewModal, setReviewModal] = useState<{
    booking: Booking
    mode: 'create' | 'edit'
    review?: Review
  } | null>(null)
  const [reviewRating, setReviewRating] = useState(5)
  const [reviewComment, setReviewComment] = useState('')

  const { data: bookings, isLoading, error } = useQuery({
    queryKey: ['bookings', 'my-bookings'],
    queryFn: async () => {
      try {
        const response = await bookingApi.getMyBookings()
        return response.data || []
      } catch (err: any) {
        toast.error(err.response?.data?.message || 'Ошибка при загрузке бронирований')
        return []
      }
    },
  })

  const cancelBookingMutation = useMutation({
    mutationFn: bookingApi.cancel,
    onSuccess: () => {
      toast.success('Бронирование отменено')
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
    },
    onError: () => {
      toast.error('Ошибка при отмене бронирования')
    },
  })

  const createReviewMutation = useMutation({
    mutationFn: (data: { bookingId: number; rating: number; comment?: string }) =>
      reviewApi.create(data),
    onSuccess: () => {
      toast.success('Отзыв добавлен')
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
      queryClient.invalidateQueries({ queryKey: ['tour'] })
      setReviewModal(null)
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Ошибка при добавлении отзыва')
    },
  })

  const updateReviewMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: { rating: number; comment?: string } }) =>
      reviewApi.update(id, data),
    onSuccess: () => {
      toast.success('Отзыв обновлён')
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
      queryClient.invalidateQueries({ queryKey: ['tour'] })
      setReviewModal(null)
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Ошибка при обновлении отзыва')
    },
  })

  const deleteReviewMutation = useMutation({
    mutationFn: reviewApi.delete,
    onSuccess: () => {
      toast.success('Отзыв удалён')
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
      queryClient.invalidateQueries({ queryKey: ['tour'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Ошибка при удалении отзыва')
    },
  })

  const openCreateReview = (booking: Booking) => {
    setReviewModal({ booking, mode: 'create' })
    setReviewRating(5)
    setReviewComment('')
  }

  const openEditReview = async (booking: Booking) => {
    if (!booking.reviewId) return
    try {
      const { data } = await reviewApi.getById(booking.reviewId)
      setReviewModal({ booking, mode: 'edit', review: data })
      setReviewRating(data.rating)
      setReviewComment(data.comment || '')
    } catch {
      toast.error('Не удалось загрузить отзыв')
    }
  }

  const submitReview = (e: React.FormEvent) => {
    e.preventDefault()
    if (!reviewModal) return
    if (reviewModal.mode === 'create') {
      createReviewMutation.mutate({
        bookingId: reviewModal.booking.id,
        rating: reviewRating,
        comment: reviewComment || undefined,
      })
    } else if (reviewModal.review) {
      updateReviewMutation.mutate({
        id: reviewModal.review.id,
        data: { rating: reviewRating, comment: reviewComment || undefined },
      })
    }
  }

  const handlePay = async (booking: Booking) => {
    if (booking.paid || booking.status === 'CANCELLED') return
    setPayingBookingId(booking.id)
    try {
      const base = window.location.origin
      const res = await paymentApi.createCheckoutSession({
        bookingId: booking.id,
        successUrl: `${base}/bookings?payment=success`,
        cancelUrl: `${base}/bookings?payment=cancelled`,
      })
      if (res.data?.url) window.location.href = res.data.url
      else toast.error('Не удалось перейти к оплате')
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Оплата недоступна')
    } finally {
      setPayingBookingId(null)
    }
  }

  const filteredBookings = bookings?.filter((booking) => {
    if (statusFilter !== 'ALL' && booking.status !== statusFilter) return false
    if (bookingSearch) {
      const search = bookingSearch.toLowerCase()
      if (!booking.tourTitle?.toLowerCase().includes(search) &&
          !booking.guideName?.toLowerCase().includes(search)) {
        return false
      }
    }
    return true
  }) || []

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center text-red-600">Ошибка при загрузке бронирований</div>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Мои бронирования</h1>

      <div className="mb-6 flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
          <input
            type="text"
            placeholder="Поиск по экскурсиям..."
            value={bookingSearch}
            onChange={(e) => setBookingSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border rounded-md"
          />
        </div>
        <div className="flex items-center gap-2">
          <Filter size={20} className="text-gray-400" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-4 py-2 border rounded-md"
          >
            <option value="ALL">Все статусы</option>
            {Object.entries(statusLabels).map(([key, label]) => (
              <option key={key} value={key}>{label}</option>
            ))}
          </select>
        </div>
      </div>

      {filteredBookings.length === 0 ? (
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
                    <Link
                      to={`/tours/${booking.tourId}`}
                      className="hover:underline hover:text-primary-700"
                    >
                      {booking.tourTitle ?? `Экскурсия #${booking.tourId}`}
                    </Link>
                  </h3>
                  {booking.guideName && (
                    <p className="text-gray-600">Гид: {booking.guideName}</p>
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

              {booking.specialRequests && (
                <div className="mb-4">
                  <p className="text-sm text-gray-600">
                    <span className="font-medium">Особые пожелания:</span> {booking.specialRequests}
                  </p>
                </div>
              )}

              {booking.status === 'COMPLETED' && (
                <div className="mb-4 p-4 bg-gray-50 rounded-lg">
                  {booking.hasReview ? (
                    <div>
                      <p className="text-sm font-medium text-gray-700 mb-2">Спасибо за отзыв!</p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => openEditReview(booking)}
                          className="flex items-center px-3 py-1.5 text-sm font-medium text-primary-600 hover:text-primary-700"
                        >
                          <Pencil className="mr-1" size={14} />
                          Изменить
                        </button>
                        <button
                          onClick={() => booking.reviewId && deleteReviewMutation.mutate(booking.reviewId)}
                          disabled={deleteReviewMutation.isPending}
                          className="flex items-center px-3 py-1.5 text-sm font-medium text-red-600 hover:text-red-700 disabled:opacity-50"
                        >
                          <Trash2 className="mr-1" size={14} />
                          Удалить
                        </button>
                      </div>
                    </div>
                  ) : (
                    <button
                      onClick={() => openCreateReview(booking)}
                      className="flex items-center px-4 py-2 text-sm font-medium text-primary-600 hover:text-primary-700"
                    >
                      <MessageSquare className="mr-2" size={16} />
                      Оставить отзыв
                    </button>
                  )}
                </div>
              )}

              <div className="flex justify-between items-center pt-4 border-t">
                <span className="text-sm text-gray-500">
                  Создано: {new Date(booking.createdAt).toLocaleString('ru-RU')}
                  {booking.paid && (
                    <span className="ml-2 text-green-600 font-medium">• Оплачено</span>
                  )}
                </span>
                <div className="flex gap-2">
                  {!booking.paid && booking.status !== 'CANCELLED' && (
                    <button
                      onClick={() => handlePay(booking)}
                      disabled={payingBookingId === booking.id}
                      className="flex items-center px-4 py-2 text-sm font-medium bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"
                    >
                      <CreditCard className="mr-1" size={16} />
                      {payingBookingId === booking.id ? 'Переход...' : 'Оплатить'}
                    </button>
                  )}
                  {booking.status === 'PENDING' && (
                    <button
                      onClick={() => cancelBookingMutation.mutate(booking.id)}
                      disabled={cancelBookingMutation.isPending}
                      className="flex items-center px-4 py-2 text-sm font-medium text-red-600 hover:text-red-700 disabled:opacity-50"
                    >
                      <X className="mr-1" size={16} />
                      Отменить
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {reviewModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <h3 className="text-xl font-semibold mb-4">
              {reviewModal.mode === 'create' ? 'Оставить отзыв' : 'Редактировать отзыв'}
            </h3>
            <p className="text-gray-600 mb-4">{reviewModal.booking.tourTitle}</p>
            <form onSubmit={submitReview} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Оценка</label>
                <div className="flex gap-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setReviewRating(star)}
                      className="p-1 focus:outline-none"
                    >
                      <Star
                        size={28}
                        className={star <= reviewRating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'}
                      />
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Комментарий</label>
                <textarea
                  value={reviewComment}
                  onChange={(e) => setReviewComment(e.target.value)}
                  className="w-full px-3 py-2 border rounded-md"
                  rows={3}
                  placeholder="Расскажите об экскурсии..."
                />
              </div>
              <div className="flex gap-2 pt-2">
                <button
                  type="submit"
                  disabled={createReviewMutation.isPending || updateReviewMutation.isPending}
                  className="flex-1 bg-primary-600 text-white py-2 rounded-md font-medium hover:bg-primary-700 disabled:opacity-50"
                >
                  {createReviewMutation.isPending || updateReviewMutation.isPending
                    ? 'Сохранение...'
                    : reviewModal.mode === 'create'
                    ? 'Отправить'
                    : 'Сохранить'}
                </button>
                <button
                  type="button"
                  onClick={() => setReviewModal(null)}
                  className="px-4 py-2 border rounded-md hover:bg-gray-50"
                >
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
