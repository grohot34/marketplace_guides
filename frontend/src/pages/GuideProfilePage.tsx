import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useParams, Link } from 'react-router-dom'
import { guideApi, reviewApi, bookingApi } from '../api/tours'
import { useAuthStore } from '../store/authStore'
import { Star, MapPin, Clock, Users, Award, Languages, FileText } from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'

export default function GuideProfilePage() {
  const { id } = useParams<{ id: string }>()
  const guideId = id ? Number(id) : 0
  const queryClient = useQueryClient()
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  const [reviewRating, setReviewRating] = useState(5)
  const [reviewComment, setReviewComment] = useState('')
  const [selectedBookingId, setSelectedBookingId] = useState<number | null>(null)

  const { data: guide, isLoading, error } = useQuery({
    queryKey: ['guide-profile', id],
    queryFn: async () => {
      const response = await guideApi.getProfile(Number(id))
      return response.data
    },
    enabled: !!id,
  })

  const { data: reviews } = useQuery({
    queryKey: ['reviews', 'guide', id],
    queryFn: async () => {
      const res = await reviewApi.getByGuide(guideId)
      return res.data
    },
    enabled: !!guideId,
  })

  const { data: myBookings } = useQuery({
    queryKey: ['bookings', 'my-bookings'],
    queryFn: async () => {
      const res = await bookingApi.getMyBookings()
      return res.data || []
    },
    enabled: isAuthenticated() && !!guideId,
  })

  const createReviewMutation = useMutation({
    mutationFn: (data: { bookingId: number; rating: number; comment?: string }) =>
      reviewApi.create(data),
    onSuccess: () => {
      toast.success('Отзыв отправлен на модерацию')
      setReviewComment('')
      setSelectedBookingId(null)
      queryClient.invalidateQueries({ queryKey: ['reviews', 'guide', id] })
      queryClient.invalidateQueries({ queryKey: ['guide-profile', id] })
      queryClient.invalidateQueries({ queryKey: ['bookings'] })
    },
    onError: (e: any) =>
      toast.error(e?.response?.data?.message || 'Не удалось отправить отзыв'),
  })

  const completedForGuideWithoutReview =
    myBookings?.filter(
      (b) =>
        b.guideId === guideId &&
        b.status === 'COMPLETED' &&
        !b.hasReview
    ) || []

  const handleSubmitReview = (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedBookingId) {
      toast.error('Выберите бронирование')
      return
    }
    createReviewMutation.mutate({
      bookingId: selectedBookingId,
      rating: reviewRating,
      comment: reviewComment || undefined,
    })
  }

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex justify-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600" />
        </div>
      </div>
    )
  }

  if (error || !guide) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <p className="text-center text-gray-600">Гид не найден</p>
      </div>
    )
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="bg-white rounded-xl shadow-md overflow-hidden">
        <div className="md:flex">
          <div className="md:w-1/3 p-8 bg-gray-50">
            <div className="flex flex-col items-center text-center">
              {guide.avatarUrl ? (
                <img
                  src={guide.avatarUrl}
                  alt={guide.fullName}
                  className="w-32 h-32 rounded-full object-cover border-4 border-white shadow-lg mb-4"
                />
              ) : (
                <div className="w-32 h-32 rounded-full bg-primary-100 flex items-center justify-center text-4xl font-bold text-primary-600 mb-4">
                  {guide.firstName?.[0]}{guide.lastName?.[0]}
                </div>
              )}
              <h1 className="text-2xl font-bold text-gray-900">{guide.fullName}</h1>
              {(guide.averageRating > 0 || guide.totalRatings > 0) && (
                <div className="flex items-center gap-1 mt-2 text-amber-600">
                  <Star className="fill-current" size={18} />
                  <span className="font-medium">{guide.averageRating.toFixed(1)}</span>
                  <span className="text-gray-500 text-sm">({guide.totalRatings} отзывов)</span>
                </div>
              )}
            </div>

            {guide.bio && (
              <div className="mt-6">
                <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide flex items-center gap-2">
                  <FileText size={16} />
                  О гиде
                </h2>
                <p className="mt-2 text-gray-700 whitespace-pre-line">{guide.bio}</p>
              </div>
            )}

            {guide.languages && (
              <div className="mt-6">
                <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide flex items-center gap-2">
                  <Languages size={16} />
                  Языки
                </h2>
                <p className="mt-2 text-gray-700">{guide.languages}</p>
              </div>
            )}

            {guide.certifications && (
              <div className="mt-6">
                <h2 className="text-sm font-semibold text-gray-500 uppercase tracking-wide flex items-center gap-2">
                  <Award size={16} />
                  Сертификаты
                </h2>
                <p className="mt-2 text-gray-700">{guide.certifications}</p>
              </div>
            )}
          </div>

          <div className="md:w-2/3 p-8">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Экскурсии гида</h2>
            {guide.tours && guide.tours.length > 0 ? (
              <div className="grid gap-4 sm:grid-cols-2">
                {guide.tours.map((tour) => (
                  <Link
                    key={tour.id}
                    to={`/tours/${tour.id}`}
                    className="flex gap-4 p-4 rounded-lg border border-gray-200 hover:border-primary-500 hover:shadow-md transition"
                  >
                    <div className="w-24 h-24 flex-shrink-0 rounded-lg bg-gray-200 overflow-hidden">
                      {tour.imageUrl ? (
                        <img
                          src={tour.imageUrl}
                          alt={tour.title}
                          className="w-full h-full object-cover"
                          loading="lazy"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-400 text-2xl font-bold">
                          {tour.title?.[0]}
                        </div>
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <h3 className="font-semibold text-gray-900 truncate">{tour.title}</h3>
                      {tour.categoryName && (
                        <p className="text-sm text-gray-500">{tour.categoryName}</p>
                      )}
                      <div className="flex flex-wrap gap-3 mt-1 text-sm text-gray-600">
                        <span className="flex items-center gap-1">
                          <Clock size={14} />
                          {tour.durationHours} ч
                        </span>
                        <span className="flex items-center gap-1">
                          <Users size={14} />
                          до {tour.maxParticipants}
                        </span>
                        {tour.location && (
                          <span className="flex items-center gap-1">
                            <MapPin size={14} />
                            {tour.location}
                          </span>
                        )}
                      </div>
                      <p className="mt-1 font-semibold text-primary-600">
                        {Number(tour.pricePerPerson).toLocaleString('ru-RU')} BYN
                        <span className="text-gray-500 font-normal text-sm"> / чел</span>
                      </p>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">Пока нет активных экскурсий</p>
            )}

            <h2 className="text-xl font-semibold text-gray-900 mt-8 mb-4">Отзывы</h2>
            {isAuthenticated() && completedForGuideWithoutReview.length > 0 && (
              <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                <h3 className="font-medium text-gray-900 mb-3">Оставить отзыв этому гиду</h3>
                <form onSubmit={handleSubmitReview} className="space-y-3">
                  <div>
                    <label className="block text-sm text-gray-700 mb-1">Бронирование</label>
                    <select
                      value={selectedBookingId ?? ''}
                      onChange={(e) => setSelectedBookingId(e.target.value ? Number(e.target.value) : null)}
                      className="w-full border rounded px-3 py-2"
                      required
                    >
                      <option value="">Выберите завершённую экскурсию</option>
                      {completedForGuideWithoutReview.map((b) => (
                        <option key={b.id} value={b.id}>
                          {b.tourTitle} — {new Date(b.tourDateTime).toLocaleDateString('ru-RU')}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm text-gray-700 mb-1">Оценка</label>
                    <div className="flex gap-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          onClick={() => setReviewRating(star)}
                          className="p-1 focus:outline-none"
                        >
                          <Star
                            size={24}
                            className={star <= reviewRating ? 'text-amber-400 fill-amber-400' : 'text-gray-300'}
                          />
                        </button>
                      ))}
                    </div>
                  </div>
                  <div>
                    <label className="block text-sm text-gray-700 mb-1">Комментарий</label>
                    <textarea
                      value={reviewComment}
                      onChange={(e) => setReviewComment(e.target.value)}
                      className="w-full border rounded px-3 py-2"
                      rows={3}
                      placeholder="Расскажите об экскурсии..."
                    />
                  </div>
                  <button
                    type="submit"
                    disabled={createReviewMutation.isPending}
                    className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"
                  >
                    {createReviewMutation.isPending ? 'Отправка...' : 'Отправить отзыв'}
                  </button>
                </form>
                <p className="text-xs text-gray-500 mt-2">Отзыв будет опубликован после модерации.</p>
              </div>
            )}

            {reviews && reviews.length > 0 ? (
              <div className="space-y-4">
                {reviews.map((rev) => (
                  <div key={rev.id} className="p-4 border border-gray-200 rounded-lg">
                    <div className="flex items-center gap-2 mb-2">
                      <div className="flex text-amber-500">
                        {[1, 2, 3, 4, 5].map((s) => (
                          <Star
                            key={s}
                            size={16}
                            className={s <= rev.rating ? 'fill-current' : 'text-gray-200'}
                          />
                        ))}
                      </div>
                      <span className="font-medium text-gray-900">{rev.customerName}</span>
                      <span className="text-sm text-gray-500">
                        {rev.tourTitle} · {new Date(rev.createdAt).toLocaleDateString('ru-RU')}
                      </span>
                    </div>
                    {rev.comment && <p className="text-gray-700 mb-2">{rev.comment}</p>}
                    {rev.response && (
                      <div className="mt-3 pl-3 border-l-2 border-primary-200 bg-primary-50/50 py-2 rounded pr-2">
                        <p className="text-sm font-medium text-gray-700">
                          Ответ {rev.respondedByName ? `от ${rev.respondedByName}` : 'администрации'}:
                        </p>
                        <p className="text-gray-700 text-sm">{rev.response}</p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500">Пока нет отзывов</p>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
