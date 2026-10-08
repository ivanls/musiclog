import { Link } from 'react-router-dom'

type Artist = {
  id: string
  name: string
  imageUrl: string | null
  spotifyUrl: string | null
}

type ArtistCardProps = {
  artist: Artist
}

function ArtistCard({ artist }: ArtistCardProps) {
  return (
    <Link to={`/artists/${artist.id}`} className="artist-card">
      <img src={artist.imageUrl ?? ''} alt={artist.name} />

      <h3>{artist.name}</h3>
    </Link>
  )
}

export default ArtistCard