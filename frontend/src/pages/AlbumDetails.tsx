
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Link } from 'react-router-dom'
import { apiFetch } from '../api/apiFetch'
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
  const { isLoggedIn } = useAuth()

  const [album, setAlbum] = useState<Album | null>(null)
  const [albumReviews, setAlbumReviews] = useState<AlbumReview[]>([])
  const [loading, setLoading] = useState(true)
  const [loadingReviews, setLoadingReviews] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showAllReviews, setShowAllReviews] = useState(false)

  //Estados para el formulario
  const[showReviewForm, setShowReviewForm] = useState(false)
  const[rating, setRating] = useState<string>('')
  const[reviewText, setReviewText] = useState<string>('')
  const[listened, setListened] = useState(true)
  const[listenedAt, setListenedAt] = useState<string>('')
  const[submitting, setSubmitting] = useState(false)
  const[formError, setFormError] = useState<string | null>(null)

async function fetchReviews() {
    if (!id) return
    setLoadingReviews(true)

      try {
        const response = await fetch(`/api/reviews/album/${id}`)

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

async function handleSubmitReview(e: React.FormEvent) {
    e.preventDefault()
    if (!id) return

    setFormError(null)

    const ratingNumber = rating === '' ? null : Number(rating)

    if (ratingNumber !== null && (ratingNumber < 0 || ratingNumber > 10)) {
      setFormError('La puntuación debe estar entre 0 y 10')
      return
    }

    if (reviewText.trim() && ratingNumber === null) {
      setFormError('No puedes publicar una reseña sin una puntuación')
      return
    }

    setSubmitting(true)

    try {
      const body = {
        spotifyAlbumId: id,
        listened: listened,
        rating: ratingNumber,
        reviewText: reviewText.trim() === '' ? null : reviewText.trim(),
        listenedAt: listenedAt === '' ? null : listenedAt,
      }

      const response = await apiFetch('/api/reviews', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText || 'Error al crear la reseña')
      }

      // Limpiamos el formulario
      setRating('')
      setReviewText('')
      setListened(true)
      setListenedAt('')
      setShowReviewForm(false)

      // Recargamos las reseñas para que aparezca la nueva
      await fetchReviews()
    } catch (err) {
      console.error(err)
      if (err instanceof Error) {
        setFormError(err.message)
      } else {
        setFormError('No se ha podido crear la reseña')
      }
    } finally {
      setSubmitting(false)
    }
  }

  useEffect(() => {
    async function fetchAlbum() {
      setLoading(true)
      setError(null)

      try {
        const response = await fetch(
          `/api/spotify/albums/${id}`
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

  const sortedReviews = [...albumReviews].sort(
    (a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )

  const uniqueByUser: AlbumReview[] = []
  const seenUsers = new Set<string>()

  for (const review of sortedReviews) {
    if (!seenUsers.has(review.username)) {
      seenUsers.add(review.username)
      uniqueByUser.push(review)
    }
  }

  const reviewsToShow = showAllReviews ? uniqueByUser : uniqueByUser.slice(0, 3)

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

      <div className="album-review-action">
        {isLoggedIn ? (
          <>
            <button
              type="button"
              onClick={() => {
                setShowReviewForm((prev) => !prev)
                setFormError(null)
              }}
            >
              {showReviewForm ? 'Cancelar' : 'Deja tu reseña'}
            </button>

            {showReviewForm && (
              <form onSubmit={handleSubmitReview} className="review-form">
                <div>
                  <label>
                    Puntuación (0 - 10)
                    <input
                      type="number"
                      min={0}
                      max={10}
                      value={rating}
                      onChange={(e) => setRating(e.target.value)}
                      placeholder="Opcional si no hay reseña"
                    />
                  </label>
                </div>

                <div>
                  <label>
                    Comentario
                    <textarea
                      value={reviewText}
                      onChange={(e) => setReviewText(e.target.value)}
                      placeholder="¿Qué te ha parecido el álbum?"
                      rows={4}
                    />
                  </label>
                </div>

                <div>
                  <label>
                    <input
                      type="checkbox"
                      checked={listened}
                      onChange={(e) => setListened(e.target.checked)}
                    />
                    Ya lo he escuchado
                  </label>
                </div>

                <div>
                  <label>
                    Fecha de escucha (opcional)
                    <input
                      type="datetime-local"
                      value={listenedAt}
                      onChange={(e) => setListenedAt(e.target.value)}
                    />
                  </label>
                </div>

                {formError && <p className="form-error">{formError}</p>}

                <button type="submit" disabled={submitting}>
                  {submitting ? 'Guardando...' : 'Publicar reseña'}
                </button>
              </form>
            )}
          </>
        ) : (
          <Link to="/login">
            Inicia sesión para poder dejar tu reseña
          </Link>
        )}
      </div>

      <section className="album-reviews">
        <h2>Reseñas</h2>

        {loadingReviews ? (
          <p>Cargando reseñas...</p>
        ) : uniqueByUser.length === 0 ? (
          <p>Aún no hay reseñas para este álbum.</p>
        ) : (
          <>
            {reviewsToShow.map((review) => (
              <article key={review.id} className="review-card">
                <h3>{review.username}</h3>
                <p>Valoración: {review.rating}/10</p>
                {review.reviewText && <p>{review.reviewText}</p>}
                <small>
                  Publicada el{' '}
                  {new Date(review.createdAt).toLocaleDateString('es-ES')}
                </small>
              </article>
            ))}

            {!showAllReviews && uniqueByUser.length > 3 && (
              <button type="button" onClick={() => setShowAllReviews(true)}>
                Ver todas las reseñas ({uniqueByUser.length})
              </button>
            )}
          </>
        )}
      </section>
    </div>
  )
}

export default AlbumDetails
