import { Link } from "react-router-dom"

type Album = {
    id: string
    name: string
    artistName: string
    imageUrl: string | null
    spotifyUrl: string | null
}

type AlbumCardProps = {
  album: Album
}

function AlbumCard({ album }: AlbumCardProps) {
  return (
    <Link to={`/albums/${album.id}`} className="album-card">
      <img src={album.imageUrl ?? ''} alt={album.name} />

      <h3>{album.name}</h3>
      <p>{album.artistName}</p>

    </Link>
)
}

export default AlbumCard