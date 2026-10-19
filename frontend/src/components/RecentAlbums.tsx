
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { apiFetch } from '../api/apiFetch'
import AlbumCard from './AlbumCard'
import './RecentAlbums.css'

type Review = {
  id: number
  spotifyAlbumId: string
  createdAt: string
}

type Album = {
  id: string
  name: string
  artistName: string
  imageUrl: string | null
  spotifyUrl: string | null
}

function RecentAlbums() {
  const { isLoggedIn } = useAuth()

  const [albums, setAlbums] = useState<Album[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    async function loadRecentAlbums() {
      if (!isLoggedIn) {
        setAlbums([])
        setLoading(false)
        setError('')
        return
      }

      setLoading(true)
      setError('')

      try {
        const reviewsResponse = await apiFetch('/api/reviews/me')

        if (!reviewsResponse.ok) {
          throw new Error('No se han podido cargar tus reseñas.')
        }

        const reviews: Review[] = await reviewsResponse.json()

        const recentReviews = [...reviews]
          .sort(
            (a, b) =>
              new Date(b.createdAt).getTime() -
              new Date(a.createdAt).getTime(),
          )
          .slice(0, 6)

        const albumResults = await Promise.all(
          recentReviews.map(async (review) => {
            const response = await apiFetch(
              `/api/spotify/albums/${review.spotifyAlbumId}`,
            )

            if (!response.ok) {
              return null
            }

            return (await response.json()) as Album
          }),
        )

        if (!cancelled) {
          setAlbums(
            albumResults.filter(
              (album): album is Album => album !== null,
            ),
          )
        }
      } catch (err) {
        if (!cancelled) {
          console.error('Error loading recent albums:', err)
          setError('No se han podido cargar tus álbumes recientes.')
          setAlbums([])
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadRecentAlbums()

    return () => {
      cancelled = true
    }
  }, [isLoggedIn])

  return (
    <section className="recent-albums">
      <h2>Tus álbumes recientes</h2>

      {!isLoggedIn ? (
        <p>
          <Link to="/login">Inicia sesión</Link> para ver tus álbumes
          recientes.
        </p>
      ) : loading ? (
        <p>Cargando tus álbumes...</p>
      ) : error ? (
        <p>{error}</p>
      ) : albums.length === 0 ? (
        <p>Aún no tienes álbumes puntuados.</p>
      ) : (
        <div className="recent-albums-grid">
          {albums.map((album) => (
            <AlbumCard key={album.id} album={album} />
          ))}
        </div>
      )}
    </section>
  )
}

export default RecentAlbums
