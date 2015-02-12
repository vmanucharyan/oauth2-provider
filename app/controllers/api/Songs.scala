package controllers.api

import data.DataProvider
import models.Song
import oauth2.AuthInfo
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Songs extends Controller {
  val pageLen = 2

  def all(page: Int) = Action.async { implicit request =>
    val from = (page - 1) * pageLen

    if (!AuthInfo.isAuthorized) Future(Unauthorized(Json.obj("error" -> "unauthorized")))
    else DataProvider.getAllSongs(from, pageLen).map { songs =>
      Ok(Json.prettyPrint(Json.obj(
        "page" -> page,
        "page_len" -> pageLen,
        "values" -> JsArray(
          for (song <- songs) yield Json.obj(
            "id" -> song.id,
            "name" -> song.name,
            "genre" -> song.genre,
            "duration_sec" -> song.durationSec,
            "album_id" -> song.albumId
          ))
        )
      ))
    }
  }

  def id(id: Int) = Action.async { implicit request =>
    if (!AuthInfo.isAuthorized) Future(Unauthorized(Json.obj("error" -> "unauthorized")))
    else DataProvider.getSong(id).map {

      case Some(s) =>
        Ok(Json.prettyPrint(JsObject(Seq(
          "id" -> JsNumber(s.id),
          "name" -> JsString(s.name),
          "genre" -> JsString(s.genre),
          "duration_sec" -> JsNumber(s.durationSec),
          "album_id" -> JsNumber(s.albumId)
        ))))

      case None => NotFound(Json.obj("error" -> s"no song with id '$id'"))

    } recover {
      case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
    }
  }

  def insertSong() = Action { implicit request =>
    if (!AuthInfo.isAuthorized) Unauthorized(JsObject(Seq("error" -> JsString("unauthorized"))))
    else request.body.asJson match {

      case Some(json) => try {
        val song = parseSong(json)
        val id = DataProvider.insertSong(song)

        Ok(JsObject(Seq(
          "message" -> JsString("success"),
          "id" -> JsNumber(id)
        )))

      } catch {
        case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
      }

      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))

    }
  }

  def update(id: Long) = Action.async { implicit request =>
    if (!AuthInfo.isAuthorized) Future(Unauthorized(JsObject(Seq("error" -> JsString("unauthorized")))))
    else request.body.asJson match {

      case Some(json) => DataProvider.getSong(id) map {

        case Some(song) =>
          val song = parseSong(json).copy(id = id)
          DataProvider.updateSong(song)
          Ok(JsObject(Seq("message" -> JsString("success"))));

        case None => NotFound(Json.obj("message" -> s"song with id='$id' not found"))
      }

      case None => Future(BadRequest(Json.obj("error" -> "empty body")))
    }
  }

  def delete(id: Long) = Action { implicit request =>
    if (!AuthInfo.isAuthorized)
      Unauthorized(Json.obj("error" -> "unauthorized"))
    else {
      DataProvider.deleteSong(id)
      Ok(Json.obj("message" -> "success"))
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
