import apiClient from './client'

export interface Tour {
  id: number
  title: string
  description?: string
  pricePerPerson: number
  durationHours: number
  maxParticipants: number
  location?: string
  meetingPoint?: string
  itinerary?: string
  imageUrl?: string
  categoryId: number
  categoryName?: string
  guideId: number
  guideName?: string
  guideBio?: string
  guideRating?: number
  active?: boolean
  averageRating?: number
  totalRatings?: number
}

export interface Review {
  id: number
  bookingId: number
  guideId: number
  guideName?: string
  tourId: number
  tourTitle?: string
  customerId?: number
  customerName?: string
  rating: number
  comment?: string
  createdAt: string
  status?: string
  response?: string
  respondedAt?: string
  respondedByName?: string
}

export interface Booking {
  id: number
  customerId: number
  customerName?: string
  tourId: number
  tourTitle?: string
  guideId: number
  guideName?: string
  tourDateTime: string
  numberOfParticipants: number
  contactPhone?: string
  specialRequests?: string
  status: 'PENDING' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
  totalPrice: number
  createdAt: string
  confirmedAt?: string
  completedAt?: string
  cancelledAt?: string
  hasReview?: boolean
  reviewId?: number
  paid?: boolean
}

export const paymentApi = {
  createCheckoutSession: (data: {
    bookingId: number
    successUrl?: string
    cancelUrl?: string
  }) =>
    apiClient.post<{ url: string }>('/payments/checkout-session', data),
  confirmSession: (sessionId: string) =>
    apiClient.post<void>('/payments/confirm-session', null, {
      params: { session_id: sessionId },
    }),
}

export const tourApi = {
  getAll: () => apiClient.get<Tour[]>('/tours'),
  getById: (id: number) => apiClient.get<Tour>(`/tours/${id}`),
  getByCategory: (categoryId: number) =>
    apiClient.get<Tour[]>(`/tours/category/${categoryId}`),
  getByGuide: (guideId: number) =>
    apiClient.get<Tour[]>(`/tours/guide/${guideId}`),
  search: (query: string) =>
    apiClient.get<Tour[]>(`/tours/search?query=${encodeURIComponent(query)}`),
  getTopRated: () => apiClient.get<Tour[]>('/tours/top-rated'),
  getMyTours: () => apiClient.get<Tour[]>('/tours/my-tours'),
  create: (data: {
    title: string
    description?: string
    pricePerPerson: number
    durationHours: number
    maxParticipants: number
    location?: string
    meetingPoint?: string
    itinerary?: string
    imageUrl?: string
    categoryId: number
    guideId?: number
  }) => apiClient.post<Tour>('/tours', data),
  update: (id: number, data: {
    title?: string
    description?: string
    pricePerPerson?: number
    durationHours?: number
    maxParticipants?: number
    location?: string
    meetingPoint?: string
    itinerary?: string
    imageUrl?: string
    categoryId?: number
  }) => apiClient.put<Tour>(`/tours/${id}`, data),
  delete: (id: number) => apiClient.delete(`/tours/${id}`),
}

export const bookingApi = {
  getAll: () => apiClient.get<Booking[]>('/bookings'),
  getById: (id: number) => apiClient.get<Booking>(`/bookings/${id}`),
  getMyBookings: () => apiClient.get<Booking[]>('/bookings/my-bookings'),
  getMyGuideBookings: () => apiClient.get<Booking[]>('/bookings/my-guide-bookings'),
  getByCustomer: (customerId: number) =>
    apiClient.get<Booking[]>(`/bookings/customer/${customerId}`),
  getByGuide: (guideId: number) =>
    apiClient.get<Booking[]>(`/bookings/guide/${guideId}`),
  getByTour: (tourId: number) =>
    apiClient.get<Booking[]>(`/bookings/tour/${tourId}`),
  create: (data: {
    tourId: number
    tourDateTime: string
    numberOfParticipants: number
    contactPhone?: string
    specialRequests?: string
  }) => apiClient.post<Booking>('/bookings', data),
  updateStatus: (id: number, status: Booking['status']) =>
    apiClient.put<Booking>(`/bookings/${id}/status?status=${status}`),
  cancel: (id: number) => apiClient.delete(`/bookings/${id}`),
}

export interface GuideStats {
  totalTours: number
  totalBookings: number
  completedBookings: number
  pendingBookings: number
  confirmedBookings: number
  inProgressBookings: number
  cancelledBookings: number
  totalReviews: number
  averageRating: number
  totalRevenue: number
  bookingsByStatus: Record<string, number>
}

export interface GuideProfile {
  id: number
  firstName: string
  lastName: string
  fullName: string
  bio?: string
  languages?: string
  certifications?: string
  averageRating: number
  totalRatings: number
  avatarUrl?: string
  tours: Tour[]
}

export const guideApi = {
  getMyStats: () => apiClient.get<GuideStats>('/guides/me/stats'),
  getProfile: (id: number) => apiClient.get<GuideProfile>(`/guides/${id}`),
}

export const reviewApi = {
  getAll: () => apiClient.get<Review[]>('/reviews'),
  getById: (id: number) => apiClient.get<Review>(`/reviews/${id}`),
  getByTour: (tourId: number) =>
    apiClient.get<Review[]>(`/reviews/tour/${tourId}`),
  getByGuide: (guideId: number) =>
    apiClient.get<Review[]>(`/reviews/guide/${guideId}`),
  create: (data: {
    bookingId: number
    rating: number
    comment?: string
  }) => apiClient.post<Review>('/reviews', data),
  update: (id: number, data: {
    rating?: number
    comment?: string
  }) => apiClient.put<Review>(`/reviews/${id}`, data),
  delete: (id: number) => apiClient.delete(`/reviews/${id}`),
}
