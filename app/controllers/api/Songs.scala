package controllers.api

import data.DataProvider
import models.Song
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Songs extends Controller {
  def all() = Action.async {
    DataProvider.getAllSongs().map { songs =>
      Ok(Json.prettyPrint(JsObject(Seq(
        "values" -> JsArray(
          for (song <- songs) yield JsObject(Seq(
            "id" -> JsNumber(song.id),
            "name" -> JsString(song.name),
            "genre" -> JsString(song.genre),
            "duration_sec" -> JsNumber(song.durationSec),
            "album_id" -> JsNumber(song.albumId)
          ))
        )
      ))))
    }
  }

  def id(id: Int) = Action.async { implicit request =>
    DataProvider.getSong(id).map {
      case Some(s) =>
        Ok(Json.prettyPrint(JsObject(Seq(
          "id" -> JsNumber(s.id),
          "name" -> JsString(s.name),
          "genre" -> JsString(s.genre),
          "duration_sec" -> JsNumber(s.durationSec),
          "album_id" -> JsNumber(s.albumId)
        ))))

      case None =>
        NotFound(JsObject(Seq(
          "error" -> JsString(s"no song with id '$id'")
        )))

    } recover {
      case ex =>
        InternalServerError(JsObject(Seq(
          "error" -> JsString(ex.getMessage)
        )))
    }
  }

  def insertSong() = Action { implicit request =>
    request.body.asJson match {

      case Some(json) =>
        try {
          val song = parseSong(json)
          DataProvider.insertSong(song)
          Ok(JsObject(Seq("message" -> JsString("success"))))
        }
        catch {
          case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
        }

      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))

    }
  }

  def update(id: Long) = Action.async { implicit request =>
    request.body.asJson match {

      case Some(json) =>

        DataProvider.getSong(id) map {
          case Some(song) =>
            val song = parseSong(json)
            DataProvider.updateSong(song)
            Ok(JsObject(Seq("message" -> JsString("success"))));

          case None =>
            NotFound(JsObject(Seq("message" -> JsString(s"song with id='$id' not found"))))

        }

      case None => Future(BadRequest(JsObject(Seq("error" -> JsString("empty body")))))

    }
  }

  private def parseSong(json: JsValue): Song = Song(
    name = (json \ "name").as[String],
    genre = (json \ "genre").as[String],
    durationSec = (json \ "duration_sec").as[Int],
    albumId = (json \ "album_id").as[Long],
    artistId = (json \ "artist_id").as[Long]
  )
}
