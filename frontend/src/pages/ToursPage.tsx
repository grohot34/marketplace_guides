import { useQuery } from '@tanstack/react-query'
import { useSearchParams, Link, useNavigate } from 'react-router-dom'
import { tourApi } from '../api/tours'
import { categoryApi } from '../api/services'
import { Star, Clock, Search, Users, SlidersHorizontal, X } from 'lucide-react'
import { useState } from 'react'

export default function ToursPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const categoryId = searchParams.get('category')
  const [searchQuery, setSearchQuery] = useState('')
  const [showFilters, setShowFilters] = useState(false)
  const [priceMin, setPriceMin] = useState<string>('')
  const [priceMax, setPriceMax] = useState<string>('')
  const [minGuideRating, setMinGuideRating] = useState<string>('')
  const [maxParticipants, setMaxParticipants] = useState<string>('')
  const [durationMin, setDurationMin] = useState<string>('')
  const [durationMax, setDurationMax] = useState<string>('')

  const { data: tours, isLoading } = useQuery({
    queryKey: ['tours', categoryId],
    queryFn: async () => {
      if (categoryId) {
        const response = await tourApi.getByCategory(Number(categoryId))
        return response.data
      }
      const response = await tourApi.getAll()
      return response.data
    },
  })

  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const response = await categoryApi.getAll()
      return response.data
    },
  })

  const filteredTours = tours?.filter((tour) => {
    if (searchQuery) {
      const query = searchQuery.toLowerCase()
      if (
        !tour.title.toLowerCase().includes(query) &&
        !tour.description?.toLowerCase().includes(query) &&
        !tour.categoryName?.toLowerCase().includes(query) &&
        !tour.guideName?.toLowerCase().includes(query) &&
        !tour.location?.toLowerCase().includes(query)
      ) {
        return false
      }
    }
    const price = Number(tour.pricePerPerson)
    if (priceMin !== '' && !isNaN(Number(priceMin)) && price < Number(priceMin)) return false
    if (priceMax !== '' && !isNaN(Number(priceMax)) && price > Number(priceMax)) return false
    const guideRating = tour.guideRating ?? 0
    if (minGuideRating !== '' && guideRating < Number(minGuideRating)) return false
    if (maxParticipants !== '' && !isNaN(Number(maxParticipants)) && tour.maxParticipants > Number(maxParticipants)) return false
    const duration = tour.durationHours
    if (durationMin !== '' && !isNaN(Number(durationMin)) && duration < Number(durationMin)) return false
    if (durationMax !== '' && !isNaN(Number(durationMax)) && duration > Number(durationMax)) return false
    return true
  }) || []

  const hasActiveFilters = priceMin !== '' || priceMax !== '' || minGuideRating !== '' || maxParticipants !== '' || durationMin !== '' || durationMax !== ''

  const clearFilters = () => {
    setPriceMin('')
    setPriceMax('')
    setMinGuideRating('')
    setMaxParticipants('')
    setDurationMin('')
    setDurationMax('')
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-4">Все экскурсии</h1>
        <div className="mb-4">
          <div className="relative max-w-md">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
            <input
              type="text"
              placeholder="Поиск экскурсий..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border rounded-md"
            />
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link
            to="/tours"
            className={`px-4 py-2 rounded-lg ${
              !categoryId
                ? 'bg-primary-600 text-white'
                : 'bg-white text-gray-700 hover:bg-gray-100'
            }`}
          >
            Все
          </Link>
          {categories?.map((category) => (
            <Link
              key={category.id}
              to={`/tours?category=${category.id}`}
              className={`px-4 py-2 rounded-lg ${
                categoryId === String(category.id)
                  ? 'bg-primary-600 text-white'
                  : 'bg-white text-gray-700 hover:bg-gray-100'
              }`}
            >
              {category.name}
            </Link>
          ))}
          <button
            type="button"
            onClick={() => setShowFilters((v) => !v)}
            className={`inline-flex items-center px-4 py-2 rounded-lg border ${
              showFilters || hasActiveFilters
                ? 'bg-primary-100 border-primary-500 text-primary-700'
                : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50'
            }`}
          >
            <SlidersHorizontal size={18} className="mr-1.5" />
            Фильтры
            {hasActiveFilters && (
              <span className="ml-1.5 bg-primary-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                !
              </span>
            )}
          </button>
        </div>
        {showFilters && (
          <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200 flex flex-wrap items-end gap-4">
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Цена, BYN</label>
              <div className="flex gap-2">
                <input
                  type="number"
                  min={0}
                  step={1}
                  placeholder="от"
                  value={priceMin}
                  onChange={(e) => setPriceMin(e.target.value)}
                  className="w-20 px-2 py-1.5 border rounded text-sm"
                />
                <input
                  type="number"
                  min={0}
                  step={1}
                  placeholder="до"
                  value={priceMax}
                  onChange={(e) => setPriceMax(e.target.value)}
                  className="w-20 px-2 py-1.5 border rounded text-sm"
                />
              </div>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Рейтинг гида</label>
              <select
                value={minGuideRating}
                onChange={(e) => setMinGuideRating(e.target.value)}
                className="px-3 py-1.5 border rounded text-sm bg-white min-w-[100px]"
              >
                <option value="">Любой</option>
                <option value="1">1+ ★</option>
                <option value="2">2+ ★</option>
                <option value="3">3+ ★</option>
                <option value="4">4+ ★</option>
                <option value="5">5 ★</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">До участников</label>
              <input
                type="number"
                min={1}
                placeholder="—"
                value={maxParticipants}
                onChange={(e) => setMaxParticipants(e.target.value)}
                className="w-24 px-2 py-1.5 border rounded text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-500 mb-1">Длительность, ч</label>
              <div className="flex gap-2">
                <input
                  type="number"
                  min={0}
                  step={0.5}
                  placeholder="от"
                  value={durationMin}
                  onChange={(e) => setDurationMin(e.target.value)}
                  className="w-16 px-2 py-1.5 border rounded text-sm"
                />
                <input
                  type="number"
                  min={0}
                  step={0.5}
                  placeholder="до"
                  value={durationMax}
                  onChange={(e) => setDurationMax(e.target.value)}
                  className="w-16 px-2 py-1.5 border rounded text-sm"
                />
              </div>
            </div>
            {hasActiveFilters && (
              <button
                type="button"
                onClick={clearFilters}
                className="inline-flex items-center px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900"
              >
                <X size={16} className="mr-1" />
                Сбросить
              </button>
            )}
          </div>
        )}
      </div>

      {isLoading ? (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      ) : filteredTours.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-600">Экскурсии не найдены</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredTours.map((tour) => (
            <Link
              key={tour.id}
              to={`/tours/${tour.id}`}
              className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
            >
              <div className="w-full h-48 bg-gray-200 flex items-center justify-center overflow-hidden">
                {tour.imageUrl ? (
                  <img
                    src={tour.imageUrl}
                    alt={tour.title}
                    className="w-full h-full object-cover"
                    loading="lazy"
                    decoding="async"
                    onError={(e) => {
                      (e.target as HTMLImageElement).style.display = 'none'
                      ;(e.target as HTMLImageElement).nextElementSibling?.classList.remove('hidden')
                    }}
                  />
                ) : null}
                <span className={tour.imageUrl ? 'hidden text-gray-500 text-sm' : 'text-gray-500 text-sm'}>
                  Экскурсия
                </span>
              </div>
              <div className="p-6">
                <h3 className="text-xl font-semibold text-gray-900 mb-2">{tour.title}</h3>
                {tour.description && (
                  <p className="text-gray-600 mb-4 line-clamp-2">{tour.description}</p>
                )}
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <p className="text-2xl font-bold text-primary-600">
                      {tour.pricePerPerson.toLocaleString('ru-RU')} BYN
                    </p>
                    <p className="text-sm text-gray-500">за человека</p>
                  </div>
                  {tour.averageRating != null && (tour.totalRatings ?? 0) > 0 && (
                    <div className="flex items-center">
                      <Star className="text-yellow-400 fill-yellow-400" size={20} />
                      <span className="ml-1 text-sm font-medium">
                        {tour.averageRating.toFixed(1)}
                      </span>
                      <span className="ml-1 text-sm text-gray-500">
                        ({tour.totalRatings})
                      </span>
                    </div>
                  )}
                </div>
                <div className="flex items-center gap-4 text-sm text-gray-600">
                  <div className="flex items-center">
                    <Clock size={14} className="mr-1" />
                    {tour.durationHours} ч
                  </div>
                  <div className="flex items-center">
                    <Users size={14} className="mr-1" />
                    до {tour.maxParticipants} чел.
                  </div>
                </div>
                {tour.location && (
                  <p className="text-sm text-gray-500 mt-2">{tour.location}</p>
                )}
                {tour.guideName && tour.guideId && (
                  <p className="text-sm text-gray-500 mt-2">
                    Гид:{' '}
                    <span
                      role="button"
                      tabIndex={0}
                      onClick={(e) => {
                        e.preventDefault()
                        e.stopPropagation()
                        navigate(`/guides/${tour.guideId}`)
                      }}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter' || e.key === ' ') {
                          e.preventDefault()
                          e.stopPropagation()
                          navigate(`/guides/${tour.guideId}`)
                        }
                      }}
                      className="text-primary-600 hover:underline cursor-pointer"
                    >
                      {tour.guideName}
                    </span>
                  </p>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
