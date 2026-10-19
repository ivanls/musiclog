
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import './Details.css'

type Artist = {
  id: string
  name: string
  imageUrl: string | null
  spotifyUrl: string | null
}

function ArtistDetails() {
  const { id } = useParams()

  const [artist, setArtist] = useState<Artist | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function fetchArtist() {
      setLoading(true)
      setError(null)

      try {
        const response = await fetch(
          `http://localhost:8081/api/spotify/artists/${id}`
        )

        if (!response.ok) {
          throw new Error('Error al obtener el artista')
        }

        const data: Artist = await response.json()
        setArtist(data)
      } catch {
        setError('No se ha podido cargar el artista')
      } finally {
        setLoading(false)
      }
    }

    if (id) {
      fetchArtist()
    }
  }, [id])

  if (loading) return <p className="detail-message">Cargando artista...</p>
  if (error) return <p className="detail-message">{error}</p>
  if (!artist) return <p className="detail-message">No se ha encontrado el artista.</p>

  return (
    <main className="artist-detail">
      <img
        src={artist.imageUrl ?? ''}
        alt={artist.name}
      />

      <div className="artist-detail-info">
        <p className="detail-label">ARTISTA</p>

        <h1>{artist.name}</h1>

        <p className="artist-detail-description">
          Explora la música de {artist.name} en MusicLog.
        </p>

        {artist.spotifyUrl && (
          <a
            href={artist.spotifyUrl}
            target="_blank"
            rel="noreferrer"
          >
            Escuchar en Spotify ↗
          </a>
        )}
      </div>
    </main>
  )
}

export default ArtistDetails
