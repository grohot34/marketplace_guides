import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { userApi } from '../api/services'
import { User as UserIcon, Mail, Phone, MapPin, Pencil, Lock, X } from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'

export default function ProfilePage() {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [profileForm, setProfileForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
  })
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const [showPasswordForm, setShowPasswordForm] = useState(false)

  const { data: user, isLoading } = useQuery({
    queryKey: ['user', 'me'],
    queryFn: async () => {
      const response = await userApi.getMe()
      return response.data
    },
  })

  const updateMeMutation = useMutation({
    mutationFn: userApi.updateMe,
    onSuccess: () => {
      toast.success('Профиль обновлён')
      queryClient.invalidateQueries({ queryKey: ['user', 'me'] })
      setEditing(false)
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Не удалось обновить профиль')
    },
  })

  const changePasswordMutation = useMutation({
    mutationFn: ({ currentPassword, newPassword }: { currentPassword: string; newPassword: string }) =>
      userApi.changePassword(currentPassword, newPassword),
    onSuccess: () => {
      toast.success('Пароль изменён')
      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      setShowPasswordForm(false)
      queryClient.invalidateQueries({ queryKey: ['user', 'me'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Ошибка смены пароля')
    },
  })

  const startEdit = () => {
    if (user) {
      setProfileForm({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        phone: user.phone || '',
        address: user.address || '',
      })
      setEditing(true)
    }
  }

  const submitProfile = (e: React.FormEvent) => {
    e.preventDefault()
    updateMeMutation.mutate(profileForm)
  }

  const submitPassword = (e: React.FormEvent) => {
    e.preventDefault()
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      toast.error('Пароли не совпадают')
      return
    }
    if (passwordForm.newPassword.length < 6) {
      toast.error('Пароль должен быть не менее 6 символов')
      return
    }
    changePasswordMutation.mutate({
      currentPassword: user?.oauthLinked ? '' : passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
    })
  }

  if (isLoading) {
    return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Профиль</h1>

      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center">
            <div className="p-4 bg-primary-100 rounded-full">
              <UserIcon className="text-primary-600" size={32} />
            </div>
            <div className="ml-4">
              <h2 className="text-2xl font-semibold text-gray-900">
                {user?.firstName} {user?.lastName}
              </h2>
              <p className="text-gray-600">@{user?.username}</p>
              {user?.oauthLinked && (
                <p className="text-sm text-gray-500 mt-1">Вход через Google привязан к этому аккаунту</p>
              )}
            </div>
          </div>
          {!editing ? (
            <button
              type="button"
              onClick={startEdit}
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              <Pencil size={16} className="mr-2" />
              Редактировать
            </button>
          ) : (
            <button
              type="button"
              onClick={() => setEditing(false)}
              className="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              <X size={16} className="mr-2" />
              Отмена
            </button>
          )}
        </div>

        {editing ? (
          <form onSubmit={submitProfile} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Имя</label>
                <input
                  type="text"
                  value={profileForm.firstName}
                  onChange={(e) => setProfileForm((p) => ({ ...p, firstName: e.target.value }))}
                  className="w-full px-3 py-2 border rounded-md"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Фамилия</label>
                <input
                  type="text"
                  value={profileForm.lastName}
                  onChange={(e) => setProfileForm((p) => ({ ...p, lastName: e.target.value }))}
                  className="w-full px-3 py-2 border rounded-md"
                  required
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                type="email"
                value={profileForm.email}
                onChange={(e) => setProfileForm((p) => ({ ...p, email: e.target.value }))}
                className="w-full px-3 py-2 border rounded-md"
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Телефон</label>
              <input
                type="text"
                value={profileForm.phone}
                onChange={(e) => setProfileForm((p) => ({ ...p, phone: e.target.value }))}
                className="w-full px-3 py-2 border rounded-md"
                placeholder="+375 (29) 123-45-67"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Адрес</label>
              <input
                type="text"
                value={profileForm.address}
                onChange={(e) => setProfileForm((p) => ({ ...p, address: e.target.value }))}
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <button
              type="submit"
              disabled={updateMeMutation.isPending}
              className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"
            >
              {updateMeMutation.isPending ? 'Сохранение...' : 'Сохранить'}
            </button>
          </form>
        ) : (
          <div className="space-y-4">
            <div className="flex items-center">
              <Mail className="mr-3 text-gray-400" size={20} />
              <span className="text-gray-700">{user?.email}</span>
            </div>
            {user?.phone && (
              <div className="flex items-center">
                <Phone className="mr-3 text-gray-400" size={20} />
                <span className="text-gray-700">{user.phone}</span>
              </div>
            )}
            {user?.address && (
              <div className="flex items-center">
                <MapPin className="mr-3 text-gray-400" size={20} />
                <span className="text-gray-700">{user.address}</span>
              </div>
            )}
          </div>
        )}
      </div>

      <div className="bg-white rounded-lg shadow-md p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-2 flex items-center">
          <Lock className="mr-2 text-gray-500" size={20} />
          Пароль
        </h3>
        {user?.oauthLinked && (
          <p className="text-sm text-gray-600 mb-4">
            Вы вошли через Google. Можете установить пароль, чтобы входить также по email и паролю.
          </p>
        )}
        {!showPasswordForm ? (
          <button
            type="button"
            onClick={() => setShowPasswordForm(true)}
            className="text-primary-600 hover:underline font-medium"
          >
            {user?.oauthLinked ? 'Установить пароль' : 'Сменить пароль'}
          </button>
        ) : (
          <form onSubmit={submitPassword} className="space-y-4 max-w-md">
            {!user?.oauthLinked && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Текущий пароль</label>
                <input
                  type="password"
                  value={passwordForm.currentPassword}
                  onChange={(e) => setPasswordForm((p) => ({ ...p, currentPassword: e.target.value }))}
                  className="w-full px-3 py-2 border rounded-md"
                  required
                  autoComplete="current-password"
                />
              </div>
            )}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                {user?.oauthLinked ? 'Новый пароль' : 'Новый пароль'}
              </label>
              <input
                type="password"
                value={passwordForm.newPassword}
                onChange={(e) => setPasswordForm((p) => ({ ...p, newPassword: e.target.value }))}
                className="w-full px-3 py-2 border rounded-md"
                required
                minLength={6}
                autoComplete="new-password"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Повторите пароль</label>
              <input
                type="password"
                value={passwordForm.confirmPassword}
                onChange={(e) => setPasswordForm((p) => ({ ...p, confirmPassword: e.target.value }))}
                className="w-full px-3 py-2 border rounded-md"
                required
                minLength={6}
                autoComplete="new-password"
              />
            </div>
            <div className="flex gap-2">
              <button
                type="submit"
                disabled={changePasswordMutation.isPending}
                className="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"
              >
                {changePasswordMutation.isPending ? 'Сохранение...' : user?.oauthLinked ? 'Установить пароль' : 'Сменить пароль'}
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowPasswordForm(false)
                  setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
                }}
                className="px-4 py-2 border rounded-md hover:bg-gray-50"
              >
                Отмена
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
