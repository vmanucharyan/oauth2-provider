package controllers.api

import data.DataProvider
import models.Album
import oauth2.AuthInfo
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Albums extends Controller {
  val pageLen = 2

  def all(page: Int) = Action.async { implicit request =>
    val from = (page - 1) * pageLen

    if (!AuthInfo.isAuthorized) {
      Future(Unauthorized(Json.obj("error" -> "Unauthorized")))
    } else DataProvider.getAllAlbums(from, pageLen).map { albums =>
      Ok(Json.prettyPrint(Json.obj(
        "page_len" -> pageLen,
        "page" -> page,
        "values" -> JsArray(
          for (album <- albums) yield Json.obj(
            "id" -> album.id,
            "name" -> album.name,
            "description" -> album.description,
            "year" -> album.year,
            "artist_id" -> album.artistId
          ))
        )
      ))
    } recover {
      case e => InternalServerError(Json.obj("error" -> e.getMessage))
    }
  }

  def ofArtist(artistId: Long) = Action.async { implicit request =>
    if (!AuthInfo.isAuthorized) {
      Future(Unauthorized("unautorized"))
    }
    else DataProvider.getAllArtistsAlbums(artistId) map { albums =>
      Ok(Json.prettyPrint(JsObject(Seq(
        "values" -> JsArray(
          for (album <- albums) yield JsObject(Seq(
            "id" -> JsNumber(album.id),
            "name" -> JsString(album.name),
            "description" -> JsString(album.description),
            "year" -> JsNumber(album.year),
            "artist_id" -> JsNumber(album.artistId)
          ))
        )
      ))))
    } recover {
      case e => InternalServerError(Json.obj("error" -> e.getMessage))
    }
  }

  def id(id: Int) = Action.async { implicit request =>
    if (!AuthInfo.isAuthorized) {
      Future(Unauthorized("unautorized"))
    } else {
      val songsFuture = DataProvider.songsOfAlbum(id)
      val albumsFuture = DataProvider.getAlbum(id)

      songsFuture flatMap { songs =>
        albumsFuture map {
          case Some(album) =>
            Ok(Json.prettyPrint(JsObject(Seq(
              "id" -> JsNumber(album.id),
              "name" -> JsString(album.name),
              "description" -> JsString(album.description),
              "songs" -> JsArray(
                for (s <- songs) yield { JsObject(Seq(
                  "id" -> JsNumber(s.id),
                  "name" -> JsString(s.name),
                  "genre" -> JsString(s.genre),
                  "duration_sec" -> JsNumber(s.durationSec)
                ))}
              )
            ))))

          case None => NotFound(Json.obj("error" -> JsString(s"no artist with id '$id'")))
        }
      }
    }
  }

  def insertAlbum() = Action { implicit request =>
    if (!AuthInfo.isAuthorized) Unauthorized(JsObject(Seq("error" -> JsString("unauthorized"))))
    else request.body.asJson match {

      case Some(json) =>

        try {
          val album = parseAlbum(json)
          DataProvider.insertAlbum(album)

          Ok(JsObject(Seq(
            "message" -> JsString("success")
          )))
        }
        catch {
          case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
        }

      case None => BadRequest(Json.obj("error" -> JsString("empty body")))
    }
  }

  def updateAlbum(id: Long) = Action { implicit request =>
    if (!AuthInfo.isAuthorized) Unauthorized(Json.obj("error" -> "unauthorized"))
    else request.body.asJson match {

      case Some(json) => try {
        val album = parseAlbum(json).copy(id = id)
        DataProvider.updateAlbum(album)

        Ok(JsObject(Seq("message" -> JsString("success"))))
      } catch {
        case e: Exception => InternalServerError(Json.obj("error" -> e.getMessage))
      }

      case None => BadRequest(Json.obj("error" -> "empty body"))
    }
  }

  def delete(id: Long) = Action { implicit request =>
    if (!AuthInfo.isAuthorized) Unauthorized(Json.obj("error" -> "unauthorized"))
    else {
      DataProvider.deleteAlbum(id)
      Ok(Json.obj("message" -> "success"))
    }
  }

  private def parseAlbum(json: JsValue): Album = Album(
    name = (json \ "name").as[String],
    description = (json \ "description").as[String],
    year = (json \ "year").as[Int],
    artistId = (json \ "artist_id").as[Long]
  )
}
