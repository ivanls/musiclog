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

    fetchTrack()
  }, [id])

  if (loading) {
    return <p>Cargando canción...</p>
  }

  if (error) {
    return <p>{error}</p>
  }

  if (!track) {
    return <p>No se ha encontrado la canción.</p>
  }

  return (
    <div className="track-detail">
      <img
        src={track.imageUrl ?? ''}
        alt={track.name}
      />

      <div>
        <h1>{track.name}</h1>
        <p>Artista: {track.artistName}</p>
        <p>Álbum: {track.albumName}</p>
        <p>Duración: {track.durationMs} ms</p>

        {track.spotifyUrl && (
          <a
            href={track.spotifyUrl}
            target="_blank"
            rel="noreferrer"
          >
            Abrir en Spotify
          </a>
        )}
      </div>
    </div>
  )
}

export default TrackDetails

