package controllers.api

import data.DataProvider
import oauth2.AuthInfo
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Albums extends Controller {
  def all() = Action.async { implicit request =>
    if (!AuthInfo.isAuthorized) {
      Future(Unauthorized("unautorized"))
    } else {
      DataProvider.getAllAlbums().map { albums =>
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
        case e =>
          InternalServerError(JsObject(Seq(
            "error" -> JsString(e.getMessage)
          )))
      }
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

          case None =>
            NotFound(JsObject(Seq(
              "error" -> JsString(s"no artist with id '$id'")
            )))
        }
      }
    }
  }
}
