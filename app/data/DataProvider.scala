package data

import models.oauth2.{OAuthApp, OAuthAppsTable}
import models._
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
//import scala.slick.driver.H2Driver.simple._
import play.api.db.slick.Config.driver.simple._


object DataProvider {
  private val users = TableQuery[UsersTable]
  private val apps = TableQuery[OAuthAppsTable]
  private val songs = TableQuery[SongsTable]
  private val albums = TableQuery[AlbumsTable]
  private val artists = TableQuery[ArtistsTable]


  // Users

  def getUserById(id: String)(implicit context: ExecutionContext): Future[Option[User]] = Future {
    DB.withSession(implicit session => {
      users.filter(u => u.email === id).firstOption
    })
  }

  def getUsers()(implicit context: ExecutionContext): Future[List[User]] = Future {
    DB.withSession(implicit s => {
      users.list
    })
  }

  def getApplication(id: String) (implicit context: ExecutionContext) : Future[Option[OAuthApp]] = Future {
    DB.withSession(implicit s => {
      apps.filter(a => a.id === id).firstOption
    })
  }

  def getApplicationSync(id: String): Option[OAuthApp] = {
    DB.withSession(implicit s => {
      apps.filter(a => a.id === id).firstOption
    })
  }

  def getUserApps(id: String) (implicit context: ExecutionContext) : Future[List[OAuthApp]] = Future {
    DB.withSession(implicit s => {
      apps.filter(app => app.userId === id).list
    })
  }

  def insertUser(user: User) (implicit context: ExecutionContext) : Future[Unit] = Future {
    DB.withSession(implicit s => {
      users.insert(user)
    })
  }

  def insertApplication(app: OAuthApp) (implicit context: ExecutionContext): Future[Unit] = Future {
    DB.withSession(implicit s => {
      apps.insert(app)
    })
  }


  // Albums

  def getAllAlbums() (implicit context: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit s => {
      albums.list
    })
  }

  def getAlbum(id: Long) (implicit context: ExecutionContext) : Future[Option[Album]] = Future {
    DB.withSession(implicit s => {
      albums.filter(a => a.id === id).firstOption
    })
  }

  def findAlbumByName(name: String) (implicit context: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit s => {
      albums.filter(a => a.name === name).list
    })
  }

  def getAllArtistsAlbums(artistId: Long) (implicit context: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit s => {
      albums.filter(e => e.artistId === artistId).list
    })
  }

  def insertAlbum(album: Album) (implicit context: ExecutionContext) : Long = {
    DB.withSession(implicit s =>
      albums.insert(album)
    )
  }

  def updateAlbum(album: Album): Unit = {
    DB.withSession(implicit s => {
      albums.filter(a => a.id === album.id).update(album)
    })
  }

  def deleteAlbum(id: Long): Unit = DB.withSession(implicit s =>
    albums.filter(a => a.id === id).delete
  )

  // Songs

  def getAllSongs() (implicit context: ExecutionContext) : Future[List[Song]] = Future {
    DB.withSession(implicit s => {
      songs.list
    })
  }

  def getSong(id: Long) (implicit context: ExecutionContext) : Future[Option[Song]] = Future {
    DB.withSession(implicit s => {
      songs.filter(e => e.id === id).firstOption
    })
  }
  
  def findSongByName(name: String) (implicit contxt: ExecutionContext) : Future[List[Song]] = Future {
    DB.withSession(implicit s => {
      songs.filter(e => e.name === name).list
    })
  }

  def songsOfAlbum(albumId: Long) (implicit context: ExecutionContext) : Future[List[Song]] = Future {
    DB.withSession(implicit s => {
      songs.filter(e => e.albumId === albumId).list
    })
  }

  def insertSong(song: Song) (implicit context: ExecutionContext) : Long = {
    DB.withSession(implicit s => {
      (songs returning songs.map(s => s.id)).insert(song)
    })
  }

  def updateSong(song: Song): Unit = {
    DB.withSession(implicit s => {
      songs.filter(s => s.id === song.id).update(song)
    })
  }

  def deleteSong(id: Long): Unit = {
    DB.withSession(implicit s => {
      songs.filter(s => s.id === id).delete
    })
  }
  
  
  // Artists

  def getAllArtists() (implicit context: ExecutionContext) : Future[List[Artist]] = Future {
    DB.withSession(implicit s => {
      artists.list
    })
  }

  def getArtist(id: Long) (implicit context: ExecutionContext) : Future[Option[Artist]] = Future {
    DB.withSession(implicit s => {
      artists.filter(e => e.id === id).firstOption
    })
  }

//  def findArtistByName(name: Long) (implicit context: ExecutionContext) : Future[List[Artist]] = Future {
//    DB.withSession(implicit s => {
//      artists.filter(e => e.name === name).list
//    })
//  }

  def insertArtist(artist: Artist) : Long = {
    DB.withSession(implicit s => {
      (artists returning artists.map(a => a.id)).insert(artist)
    })
  }

  def updateArtist(artist: Artist) : Unit = {
    DB.withSession(implicit s => {
      artists.filter(a => a.id === artist.id).update(artist)
    })
  }

  def deleteArtist(id: Long) : Unit = {
    DB.withSession(implicit s => {
      artists.filter(a => a.id === id).delete
    })
  }
}
