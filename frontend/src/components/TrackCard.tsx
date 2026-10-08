import { Link } from "react-router-dom"

type Track = {
    id: string
    name: string
    artistName: string
    albumName: string
    durationMs: number
    imageUrl: string | null
    spotifyUrl: string | null
}

type TrackCardProps = {
  track: Track
}

function TrackCard({ track }: TrackCardProps) {
  return (
    <Link to={`/tracks/${track.id}`} className="track-card">
      <img src={track.imageUrl ?? ''} alt={track.name} />

      <h3>{track.name}</h3>
      <p>{track.artistName}</p>
      <p>{track.albumName}</p>
      <p>{track.durationMs}</p>

    </Link>
)
}

export default TrackCard