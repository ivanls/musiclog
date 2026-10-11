import { useState } from 'react'

function Register() {
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')

async function handleRegister(e: React.SyntheticEvent<HTMLFormElement>) {
  e.preventDefault()
  setMessage('')

  try {
    const response = await fetch('/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        username,
        email,
        password,
      }),
    })

    const data = await response.json()

    if (!response.ok) {
      setMessage(
        Object.values(data).join(', ') || 'Registration failed',
      )
      return
    }

    setMessage(`Account created: ${data.username}`)
  } catch (error) {
        console.error('Registration error:', error)
        setMessage('Could not connect to the server')
}
}

  return (
  <div>
    <h2>Create account</h2>

    <form onSubmit={handleRegister}>
      <div>
        <label htmlFor="username">Username</label>
        <input
          id="username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
      </div>

      <div>
        <label htmlFor="email">Email</label>
        <input
          id="email"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
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
          minLength={8}
        />
      </div>

      <button type="submit">Register</button>
    </form>
    {message && <p>{message}</p>}
  </div>
)
}

export default Register