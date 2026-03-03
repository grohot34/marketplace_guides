import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { categoryApi } from '../api/services'

export default function HomePage() {
  const { data: categories, isLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const response = await categoryApi.getAll()
      return response.data
    },
  })

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          ГидПоинт — экскурсии с местными гидами
        </h1>
        <p className="text-xl text-gray-600">
          Откройте для себя уникальные экскурсии с опытными гидами
        </p>
      </div>

      {isLoading ? (
        <div className="text-center py-12">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {categories?.map((category) => (
            <Link
              key={category.id}
              to={`/tours?category=${category.id}`}
              className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
            >
              <h3 className="text-xl font-semibold text-gray-900 mb-2">{category.name}</h3>
              {category.description && (
                <p className="text-gray-600 mb-2">{category.description}</p>
              )}
              {category.serviceCount !== undefined && (
                <p className="text-sm text-gray-500">
                  {category.serviceCount} {category.serviceCount === 1 ? 'экскурсия' : 'экскурсий'}
                </p>
              )}
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
















