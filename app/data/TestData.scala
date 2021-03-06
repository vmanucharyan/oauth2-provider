package data

import models.{Song, Album, Artist}
import scala.concurrent.ExecutionContext.Implicits.global

object TestData {
  def fill(): Unit = {

    DataProvider.getArtist(1) map {
      case None =>
        val ffId = DataProvider.insertArtist(new Artist(
          name = "Foo Fighters",
          description =
            """Foo Fighters — американская альтернативная рок-группа,
              |образованная бывшим участником рок-группы Nirvana Дейвом
              | Гролом в 1995 году.""".stripMargin.replaceAll("\n", " "),

          id = 1
        ))

        val wlId = DataProvider.insertAlbum(new Album(
          name = "Wasting Lights",
          year = 2010,
          description =
            """Wasting Light (в пер. с англ. Испепеляющий свет) — седьмой студийный альбом
              |американской группы Foo Fighters, исполняющей альтернативный рок,
              |выпущен звукозаписывающей компанией RCA Records в апреле 2011 года.
              |Его название взято из текста песни «Miss the Misery». В записи пластинки
              |приняли участие Боб Моулд и Крист Новоселич, а Пэт Смир снова официально
              |значится участником коллектива — впервые после выпуска диска
              |The Colour and the Shape (1997), хотя он играл на концертах Foo Fighters
              |ещё с 2006 года.""".stripMargin.replaceAll("\n", " "),

          artistId = ffId,
          id = 1
        ))

        DataProvider.insertSong(new Song(
          name = "Rope",
          genre = "Rock",
          durationSec = 259,
          artistId = ffId,
          albumId = wlId
        ))

        DataProvider.insertSong(new Song(
          name = "Bridge Burning",
          genre = "Rock",
          durationSec = 284,
          artistId = ffId,
          albumId = wlId
        ))

        DataProvider.insertSong(new Song(
          name = "Back & Forth",
          genre = "Rock",
          durationSec = 234,
          artistId = ffId,
          albumId = wlId
        ))

        DataProvider.insertSong(new Song(
          name = "Walk",
          genre = "Rock",
          durationSec = 256,
          artistId = ffId,
          albumId = wlId
        ))



        val echoesId = DataProvider.insertAlbum(new Album(
          name = "Echoes, Silence, Patience & Grace",
          year = 2010,
          description =
            """Echoes, Silence, Patience & Grace — шестой студийный альбом американской рок-группы Foo Fighters,
              |выпущенный 25 сентября 2007 года. Альбом продюсировал Гил Нортон, уже работавший
              |с группой над её вторым альбомом, The Colour and the Shape.""".stripMargin.replaceAll("\n", " "),

          artistId = ffId,
          id = 2
        ))

        DataProvider.insertSong(Song(
          name = "The Pretender",
          genre = "Rock",
          durationSec = 256,
          artistId = ffId,
          albumId = echoesId
        ))

        DataProvider.insertSong(Song(
          name = "Long Road to Ruin",
          genre = "Rock",
          durationSec = 234,
          artistId = ffId,
          albumId = echoesId
        ))

        DataProvider.insertSong(Song(
          name = "Home",
          genre = "Rock",
          durationSec = 278,
          artistId = ffId,
          albumId = echoesId
        ))

      case Some(_) =>
    }
  }
}
