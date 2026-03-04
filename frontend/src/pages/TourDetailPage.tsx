import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { tourApi, bookingApi, reviewApi, paymentApi } from '../api/tours'
import { userApi } from '../api/services'
import { useAuthStore } from '../store/authStore'
import { Star, Clock, MapPin, Users, Pencil, Trash2 } from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'

export default function TourDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
  const queryClient = useQueryClient()

  const [tourDateTime, setTourDateTime] = useState('')
  const [numberOfParticipants, setNumberOfParticipants] = useState(1)
  const [contactPhone, setContactPhone] = useState('')
  const [specialRequests, setSpecialRequests] = useState('')

  const { data: tour, isLoading } = useQuery({
    queryKey: ['tour', id],
    queryFn: async () => {
      const response = await tourApi.getById(Number(id))
      return response.data
    },
  })

  const { data: reviews } = useQuery({
    queryKey: ['reviews', 'tour', id],
    queryFn: async () => {
      const response = await reviewApi.getByTour(Number(id))
      return response.data
    },
  })

  const { data: currentUser } = useQuery({
    queryKey: ['user', 'me'],
    queryFn: async () => {
      const response = await userApi.getMe()
      return response.data
    },
    enabled: isAuthenticated(),
  })

  const deleteReviewMutation = useMutation({
    mutationFn: reviewApi.delete,
    onSuccess: () => {
      toast.success('Отзыв удалён')
      queryClient.invalidateQueries({ queryKey: ['reviews'] })
      queryClient.invalidateQueries({ queryKey: ['tour', id] })
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
    },
    onError: (err: any) => toast.error(err?.response?.data?.message || 'Ошибка при удалении'),
  })

  const createBookingMutation = useMutation({
    mutationFn: bookingApi.create,
    onSuccess: async (response) => {
      const booking = response.data
      if (!booking?.id) {
        toast.success('Бронирование создано успешно!')
        queryClient.invalidateQueries({ queryKey: ['bookings'] })
        navigate('/bookings')
        return
      }
      try {
        const base = window.location.origin
        const payRes = await paymentApi.createCheckoutSession({
          bookingId: booking.id,
          successUrl: `${base}/bookings?payment=success`,
          cancelUrl: `${base}/tours/${id}?payment=cancelled`,
        })
        const url = payRes.data?.url
        if (url) {
          toast.success('Переход к оплате...')
          window.location.href = url
          return
        }
      } catch (err: any) {
        toast.error(err?.response?.data?.message || 'Оплата недоступна. Бронирование создано.')
      }
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
      navigate('/bookings')
    },
    onError: (error: any) => {
      const errorMessage = error?.response?.data?.message || error?.message || 'Ошибка при создании бронирования'
      toast.error(errorMessage)
    },
  })

  const handleBooking = (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!isAuthenticated()) {
      toast.error('Необходимо войти в систему')
      navigate('/login')
      return
    }

    if (!tourDateTime) {
      toast.error('Выберите дату и время экскурсии')
      return
    }

    if (numberOfParticipants < 1 || (tour && numberOfParticipants > tour.maxParticipants)) {
      toast.error(`Количество участников должно быть от 1 до ${tour?.maxParticipants}`)
      return
    }

    createBookingMutation.mutate({
      tourId: Number(id),
      tourDateTime: tourDateTime,
      numberOfParticipants: numberOfParticipants,
      contactPhone: contactPhone || undefined,
      specialRequests: specialRequests || undefined,
    })
  }

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      </div>
    )
  }

  if (!tour) {
    return <div>Экскурсия не найдена</div>
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div>
          <div className="w-full h-96 bg-gray-200 rounded-lg mb-6 flex items-center justify-center overflow-hidden">
            {tour.imageUrl ? (
              <img
                src={tour.imageUrl}
                alt={tour.title}
                className="w-full h-full object-cover rounded-lg"
                loading="lazy"
                decoding="async"
                onError={(e) => {
                  (e.target as HTMLImageElement).style.display = 'none'
                  ;(e.target as HTMLImageElement).nextElementSibling?.classList.remove('hidden')
                }}
              />
            ) : null}
            <span className={tour.imageUrl ? 'hidden text-gray-500' : 'text-gray-500'}>Экскурсия</span>
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-4">{tour.title}</h1>
          <div className="flex items-center gap-4 mb-4">
            {tour.averageRating != null && (tour.totalRatings ?? 0) > 0 && (
              <div className="flex items-center">
                <Star className="text-yellow-400 fill-yellow-400" size={20} />
                <span className="ml-1 font-medium">{tour.averageRating.toFixed(1)}</span>
                <span className="ml-1 text-gray-500">({tour.totalRatings} отзывов)</span>
              </div>
            )}
            <div className="flex items-center text-gray-600">
              <Clock size={18} className="mr-1" />
              {tour.durationHours} часов
            </div>
            <div className="flex items-center text-gray-600">
              <Users size={18} className="mr-1" />
              До {tour.maxParticipants} человек
            </div>
          </div>
          {tour.description && (
            <div className="mb-6">
              <h2 className="text-xl font-semibold mb-2">Описание</h2>
              <p className="text-gray-700 whitespace-pre-line">{tour.description}</p>
            </div>
          )}
          {tour.itinerary && (
            <div className="mb-6">
              <h2 className="text-xl font-semibold mb-2">Программа экскурсии</h2>
              <p className="text-gray-700 whitespace-pre-line">{tour.itinerary}</p>
            </div>
          )}
          {tour.location && (
            <div className="mb-6">
              <h2 className="text-xl font-semibold mb-2">Место проведения</h2>
              <div className="flex items-start">
                <MapPin className="text-gray-400 mr-2 mt-1" size={20} />
                <p className="text-gray-700">{tour.location}</p>
              </div>
            </div>
          )}
          {tour.meetingPoint && (
            <div className="mb-6">
              <h2 className="text-xl font-semibold mb-2">Место встречи</h2>
              <p className="text-gray-700">{tour.meetingPoint}</p>
            </div>
          )}
          {tour.guideName && tour.guideId && (
            <div className="mb-6">
              <h2 className="text-xl font-semibold mb-2">Гид</h2>
              <Link
                to={`/guides/${tour.guideId}`}
                className="text-primary-600 hover:text-primary-700 font-medium hover:underline"
              >
                {tour.guideName}
              </Link>
              {tour.guideBio && (
                <p className="text-gray-600 mt-1">{tour.guideBio}</p>
              )}
              {tour.guideRating != null && tour.guideRating > 0 && (
                <div className="flex items-center mt-2">
                  <Star className="text-yellow-400 fill-yellow-400" size={16} />
                  <span className="ml-1 text-sm">{tour.guideRating.toFixed(1)}</span>
                </div>
              )}
            </div>
          )}
        </div>

        <div>
          <div className="bg-white rounded-lg shadow-md p-6 lg:sticky lg:top-4">
            <div className="mb-6">
              <p className="text-3xl font-bold text-primary-600 mb-2">
                {tour.pricePerPerson.toLocaleString('ru-RU')} BYN
              </p>
              <p className="text-gray-600">за человека</p>
            </div>

            <form onSubmit={handleBooking} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Дата и время экскурсии
                </label>
                <input
                  type="datetime-local"
                  value={tourDateTime}
                  onChange={(e) => setTourDateTime(e.target.value)}
                  className="w-full px-3 py-2 border rounded-md"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Количество участников
                </label>
                <input
                  type="number"
                  min="1"
                  max={tour.maxParticipants}
                  value={numberOfParticipants}
                  onChange={(e) => setNumberOfParticipants(Number(e.target.value))}
                  className="w-full px-3 py-2 border rounded-md"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Контактный телефон
                </label>
                <input
                  type="tel"
                  value={contactPhone}
                  onChange={(e) => setContactPhone(e.target.value)}
                  className="w-full px-3 py-2 border rounded-md"
                  placeholder="+375 (29) 123-45-67"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Особые пожелания
                </label>
                <textarea
                  value={specialRequests}
                  onChange={(e) => setSpecialRequests(e.target.value)}
                  className="w-full px-3 py-2 border rounded-md"
                  rows={3}
                  placeholder="Дополнительная информация..."
                />
              </div>

              <div className="pt-4 border-t">
                <p className="text-sm text-gray-600 mb-2">
                  Итого: <span className="font-bold text-lg">
                    {(tour.pricePerPerson * numberOfParticipants).toLocaleString('ru-RU')} BYN
                  </span>
                </p>
                <button
                  type="submit"
                  disabled={createBookingMutation.isPending}
                  className="w-full bg-primary-600 text-white py-3 rounded-md font-medium hover:bg-primary-700 disabled:opacity-50"
                >
                  {createBookingMutation.isPending ? 'Обработка...' : 'Забронировать'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <div className="mt-10">
        <h2 className="text-2xl font-semibold mb-4">Отзывы</h2>
        {reviews && reviews.length > 0 ? (
          <div className="space-y-4">
            {reviews.map((review) => (
              <div key={review.id} className="bg-white rounded-lg shadow p-4">
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <div className="flex">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star
                          key={star}
                          size={16}
                          className={star <= review.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'}
                        />
                      ))}
                    </div>
                    {review.customerName && (
                      <span className="font-medium text-gray-700">{review.customerName}</span>
                    )}
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-gray-500">
                      {new Date(review.createdAt).toLocaleDateString('ru-RU')}
                    </span>
                    {currentUser && review.customerId === currentUser.id && (
                      <div className="flex gap-1">
                        <Link
                          to="/bookings"
                          className="p-1 text-gray-500 hover:text-primary-600"
                          title="Редактировать отзыв"
                        >
                          <Pencil size={14} />
                        </Link>
                        <button
                          onClick={() => deleteReviewMutation.mutate(review.id)}
                          disabled={deleteReviewMutation.isPending}
                          className="p-1 text-gray-500 hover:text-red-600 disabled:opacity-50"
                          title="Удалить отзыв"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    )}
                  </div>
                </div>
                {review.comment && (
                  <p className="text-gray-700">{review.comment}</p>
                )}
                {review.response && (
                  <div className="mt-3 pl-3 border-l-2 border-primary-200 bg-primary-50/50 py-2 rounded pr-2">
                    <p className="text-sm font-medium text-gray-700">
                      Ответ {review.respondedByName ? `от ${review.respondedByName}` : 'администрации'}:
                    </p>
                    <p className="text-gray-700 text-sm">{review.response}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <p className="text-gray-500">Пока нет отзывов. Будьте первым!</p>
        )}
        {isAuthenticated() && (
          <p className="mt-4 text-sm text-gray-600">
            Оставить отзыв можно после завершённой экскурсии в разделе{' '}
            <Link to="/bookings" className="text-primary-600 hover:underline">Мои бронирования</Link>.
          </p>
        )}
      </div>
    </div>
  )
}
