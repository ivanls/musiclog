
import SearchBar from '../components/SearchBar'
import './Home.css'
import RecentAlbums from '../components/RecentAlbums'

function Home() {
  return (
    <main className="home-page">
      <section className="home-intro">
        <p className="home-eyebrow">YOUR MUSIC JOURNAL</p>
        <h1>Tu próxima obsesión musical empieza aquí.</h1>
        <p>
          Descubre álbumes, comparte tus opiniones y lleva un registro
          de la música que te acompaña.
        </p>

        <SearchBar />
      </section>

      <section className="home-section">
        <div className="home-section-heading">
          <RecentAlbums />
        </div>
      </section>

      <section className="home-section">
        <div className="home-section-heading">
          <h2>Para descubrir</h2>
          <p>Encuentra tu próximo álbum favorito.</p>
        </div>

        <p className="home-empty-state">
          Próximamente: una selección de álbumes para descubrir.
        </p>
      </section>

      <section className="home-section">
        <div className="home-section-heading">
          <h2>La comunidad</h2>
          <p>Opiniones y recomendaciones de otros oyentes.</p>
        </div>

        <p className="home-empty-state">
          Aquí aparecerán las reseñas más recientes.
        </p>
      </section>
    </main>
  )
}

export default Home
