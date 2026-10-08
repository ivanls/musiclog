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

function AlbumDetails() {
  const { id } = useParams()

  const [album, setAlbum] = useState<Album | null>(null)
  const [loading, setLoading] = useState(true)
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

    fetchAlbum()
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
      <img
        src={album.imageUrl ?? ''}
        alt={album.name}
      />

      <div>
        <h1>{album.name}</h1>
        <p>Artista: {album.artistName}</p>

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
    </div>
  )
}

export default AlbumDetails
