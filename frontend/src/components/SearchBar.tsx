import { useState } from "react"
import ArtistCard from "./ArtistCard"
import TrackCard from "./TrackCard"
import AlbumCard from "./AlbumCard"
import SearchResults from "./SearchResults"

function SearchBar() {

  const [query, setQuery] = useState('')
  const [artists, setArtists] = useState<Artist[]>([])
  const [albums, setAlbums] = useState<Album[]>([])
  const [tracks, setTracks] = useState<Track[]>([])    

  async function handleSearch() {
    console.log("Buscando:", query)

    const response = await fetch(
      `http://localhost:8081/api/spotify/search?q=${query}`
    )

    const data = await response.json()

    setArtists(data.artists)
    setTracks(data.tracks)
    setAlbums(data.albums)
  }

  return (
    <div>
      <input
        type="text"
        placeholder="Search music..."
        onChange={(event) => setQuery(event.target.value)}
      />

      <p>{query}</p>

      <button onClick={handleSearch}>Search</button>

<SearchResults
  artists={artists}
  albums={albums}
  tracks={tracks}
/>

    </div>
  )
}

export default SearchBar