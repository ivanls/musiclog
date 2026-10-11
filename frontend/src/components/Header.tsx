
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

type HeaderProps = {
  title: string
}

function Header({ title }: HeaderProps) {
  const { isLoggedIn, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <header>
      <h1>{title}</h1>

      <nav>
        <Link to="/">Discover</Link>

        {isLoggedIn ? (
          <button type="button" onClick={handleLogout}>
            Logout
          </button>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </nav>
    </header>
  )
}

export default Header
