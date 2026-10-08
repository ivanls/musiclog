import ArtistCard from "./ArtistCard"
import AlbumCard from "./AlbumCard"
import TrackCard from "./TrackCard"

type SearchResultsProps = {
  artists: Artist[]
  albums: Album[]
  tracks: Track[]
}

function SearchResults({ artists, albums, tracks }: SearchResultsProps) {
  return (
    <div>

      <section className="results-section">
        <h2>Artists</h2>

        <div className="results-grid">
          {artists.map((artist) => (
            <ArtistCard key={artist.id} artist={artist} />
          ))}
        </div>
      </section>

      <section className="results-section">
        <h2>Albums</h2>

        <div className="results-grid">
          {albums.map((album) => (
            <AlbumCard key={album.id} album={album} />
          ))}
        </div>
      </section>

      <section className="results-section">
        <h2>Tracks</h2>

        <div className="results-grid">
          {tracks.map((track) => (
            <TrackCard key={track.id} track={track} />
          ))}
        </div>
      </section>

    </div>
  )
}

export default SearchResults