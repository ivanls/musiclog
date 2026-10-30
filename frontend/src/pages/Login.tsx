import { useState } from 'react'
import type { SyntheticEvent } from 'react'
import { useAuth } from '../context/AuthContext'
import { apiFetch } from '../api/apiFetch'

function Login() {
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const [token, setToken] = useState('')
  const { login } = useAuth()
 
  async function handleLogin(e: SyntheticEvent<HTMLFormElement>) {
    e.preventDefault()
    setMessage('')
    setToken('')

    try {
      const response = await fetch('/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          identifier,
          password,
        }),
      })

      const data = await response.json()

      if (!response.ok) {
        setMessage(
          Object.values(data).join(', ') || 'Invalid credentials',
        )
        return
      }

        login(data.token)
        setToken(data.token)
        setMessage('Login successful!')
        
    } catch (error) {
      console.error('Login error:', error)
      setMessage('Could not connect to the server')
    }
  }

  return (
    <div>
      <h2>Login</h2>

      <form onSubmit={handleLogin}>
        <div>
          <label htmlFor="identifier">Username or email</label>
          <input
            id="identifier"
            type="text"
            value={identifier}
            onChange={(e) => setIdentifier(e.target.value)}
            required
          />
        </div>

        <div>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>

        <button type="submit">Login</button>
      </form>

      {message && <p>{message}</p>}

      {token && (
        <p>Authentication token saved successfully.</p>
      )}
    </div>
  )
}

export default Login