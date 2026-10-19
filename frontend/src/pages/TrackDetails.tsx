
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import './Details.css'

type Track = {
  id: string
  name: string
  artistName: string
  albumName: string
  durationMs: number
  imageUrl: string | null
  spotifyUrl: string | null
}

function formatDuration(durationMs: number): string {
  const totalSeconds = Math.floor(durationMs / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60

  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

function TrackDetails() {
  const { id } = useParams()
  const [track, setTrack] = useState<Track | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function fetchTrack() {
      setLoading(true)
      setError(null)

      try {
        const response = await fetch(
          `http://localhost:8081/api/spotify/tracks/${id}`
        )

        if (!response.ok) {
          throw new Error('Error al obtener la canción')
        }

        const data: Track = await response.json()
        setTrack(data)
      } catch {
        setError('No se ha podido cargar la canción')
      } finally {
        setLoading(false)
      }
    }

    if (id) {
      fetchTrack()
    }
  }, [id])

  if (loading) return <p>Cargando canción...</p>
  if (error) return <p>{error}</p>
  if (!track) return <p>No se ha encontrado la canción.</p>

  return (
    <div className="track-detail">
      <img src={track.imageUrl ?? ''} alt={track.albumName} />

      <div>
        <p className="detail-label">CANCIÓN</p>

        <h1>{track.name}</h1>

        <p>
          <strong>Artista:</strong> {track.artistName}
        </p>

        <p>
          <strong>Álbum:</strong> {track.albumName}
        </p>

        <p>
          <strong>Duración:</strong> {formatDuration(track.durationMs)}
        </p>

        {track.spotifyUrl && (
          <a
            href={track.spotifyUrl}
            target="_blank"
            rel="noreferrer"
          >
            Escuchar en Spotify
          </a>
        )}
      </div>
    </div>
  )
}

export default TrackDetails
