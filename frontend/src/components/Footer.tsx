import { Link } from 'react-router-dom'
import { Home, MapPin, Mail, Phone } from 'lucide-react'

export default function Footer() {
  const currentYear = new Date().getFullYear()

  return (
    <footer className="bg-gray-800 text-gray-300 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div>
            <Link to="/" className="flex items-center text-white font-bold text-lg hover:text-primary-400 transition-colors">
              <Home className="mr-2" size={22} />
              ГидПоинт
            </Link>
            <p className="mt-3 text-sm">
              Экскурсии и туры по Беларуси. Выбирайте гидов и бронируйте поездки онлайн.
            </p>
          </div>
          <div>
            <h3 className="text-white font-semibold mb-3">Навигация</h3>
            <ul className="space-y-2 text-sm">
              <li>
                <Link to="/" className="hover:text-primary-400 transition-colors">Главная</Link>
              </li>
              <li>
                <Link to="/tours" className="hover:text-primary-400 transition-colors">Экскурсии</Link>
              </li>
              <li>
                <Link to="/login" className="hover:text-primary-400 transition-colors">Войти</Link>
              </li>
              <li>
                <Link to="/register" className="hover:text-primary-400 transition-colors">Регистрация</Link>
              </li>
            </ul>
          </div>
          <div>
            <h3 className="text-white font-semibold mb-3">Контакты</h3>
            <ul className="space-y-2 text-sm">
              <li className="flex items-center gap-2">
                <MapPin size={16} className="shrink-0 text-primary-400" />
                Минск, Беларусь
              </li>
              <li className="flex items-center gap-2">
                <Phone size={16} className="shrink-0 text-primary-400" />
                <a href="tel:+375291234567" className="hover:text-primary-400 transition-colors">+375 (29) 123-45-67</a>
              </li>
              <li className="flex items-center gap-2">
                <Mail size={16} className="shrink-0 text-primary-400" />
                <a href="mailto:info@guidpoint.by" className="hover:text-primary-400 transition-colors">info@guidpoint.by</a>
              </li>
            </ul>
          </div>
        </div>
        <div className="mt-8 pt-8 border-t border-gray-700 text-center text-sm text-gray-500">
          © {currentYear} ГидПоинт. Все права защищены.
        </div>
      </div>
    </footer>
  )
}
