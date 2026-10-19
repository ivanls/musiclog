
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import './Details.css'

type Album = {
  id: string
  name: string
  artistName: string
  imageUrl: string | null
  spotifyUrl: string | null
}

type AlbumReview = {
  id: number
  username: string
  spotifyAlbumId: string
  rating: number
  reviewText: string | null
  createdAt: string
  updatedAt: string
}

function AlbumDetails() {
  const { id } = useParams()

  const [album, setAlbum] = useState<Album | null>(null)
  const [albumReviews, setAlbumReviews] = useState<AlbumReview[]>([])
  const [loading, setLoading] = useState(true)
  const [loadingReviews, setLoadingReviews] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function fetchAlbum() {
      setLoading(true)
      setError(null)

      try {
        const response = await fetch(
          `http://localhost:8081/api/spotify/albums/${id}`
        )

        if (!response.ok) {
          throw new Error('Error al obtener el álbum')
        }

        const data: Album = await response.json()
        setAlbum(data)
      } catch {
        setError('No se ha podido cargar el álbum')
      } finally {
        setLoading(false)
      }
    }

    async function fetchReviews() {
      setLoadingReviews(true)

      try {
        const response = await fetch(
          `http://localhost:8081/api/reviews/album/${id}`
        )

        if (!response.ok) {
          throw new Error('Error al obtener las reseñas')
        }

        const data: AlbumReview[] = await response.json()
        setAlbumReviews(data)
      } catch (error) {
        console.error('No se han podido cargar las reseñas:', error)
      } finally {
        setLoadingReviews(false)
      }
    }

    if (id) {
      fetchAlbum()
      fetchReviews()
    }
  }, [id])

  if (loading) {
    return <p>Cargando álbum...</p>
  }

  if (error) {
    return <p>{error}</p>
  }

  if (!album) {
    return <p>No se ha encontrado el álbum.</p>
  }

  return (
    <div className="album-detail">
      <img src={album.imageUrl ?? ''} alt={album.name} />

      <div>
        <h1>{album.name}</h1>
        <p className="detail-label">Artista: {album.artistName}</p>

        {album.spotifyUrl && (
          <a
            href={album.spotifyUrl}
            target="_blank"
            rel="noreferrer"
          >
            Abrir en Spotify
          </a>
        )}
      </div>

      <section className="album-reviews">
        <h2>Reseñas</h2>

        {loadingReviews ? (
          <p>Cargando reseñas...</p>
        ) : albumReviews.length === 0 ? (
          <p>Aún no hay reseñas para este álbum.</p>
        ) : (
          albumReviews.map((review) => (
            <article key={review.id} className="review-card">
              <h3>{review.username}</h3>
              <p>Valoración: {review.rating}/10</p>
              {review.reviewText && <p>{review.reviewText}</p>}
              <small>
                Publicada el{' '}
                {new Date(review.createdAt).toLocaleDateString('es-ES')}
              </small>
            </article>
          ))
        )}
      </section>
    </div>
  )
}

export default AlbumDetails
