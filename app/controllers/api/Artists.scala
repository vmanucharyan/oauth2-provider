package controllers.api


import data.DataProvider
import models.Artist
import oauth2.AuthInfo
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Artists extends Controller {
  def all() = Action.async { implicit request =>
    if (!AuthInfo.isAuthorized) Future(Unauthorized(JsObject(Seq("error" -> JsString("unauthorized")))))
    else DataProvider.getAllArtists() map { artists =>
      Ok(Json.prettyPrint(JsObject(Seq(
        "values" -> JsArray(
          for (artist <- artists) yield JsObject(Seq(
            "id" -> JsNumber(artist.id),
            "name" -> JsString(artist.name),
            "description" -> JsString(artist.description)
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

  def id(id: Int) = Action.async { implicit request =>
    if (!AuthInfo.isAuthorized) Future(Unauthorized(JsObject(Seq("error" -> JsString("unauthorized")))))
    else {
      DataProvider.getArtist(id) map {
        case Some(artist) =>
          Ok(Json.prettyPrint(JsObject(Seq(
            "id" -> JsNumber(artist.id),
            "name" -> JsString(artist.name),
            "description" -> JsString(artist.description)
          ))))

        case None =>
          NotFound(JsObject(Seq(
            "error" -> JsString(s"no artist with id '$id'")
          )))
      } recover {
        case e =>
          InternalServerError(JsObject(Seq(
            "error" -> JsString(e.getMessage)
          )))
      }
    }
  }

  def insertArtist() = Action { implicit request =>
    if (!AuthInfo.isAuthorized) Unauthorized(JsObject(Seq("error" -> JsString("unauthorized"))))
    else request.body.asJson match {
      case Some(json) => try {
          val artist = parseArtist(json)
          val id = DataProvider.insertArtist(artist)
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

  def updateArtist(id: Long) = Action { implicit request =>
    if (!AuthInfo.isAuthorized) Unauthorized(JsObject(Seq("error" -> JsString("unauthorized"))))
    else request.body.asJson match {

      case Some(json) => try {

        val artist = parseArtist(json).copy(id = id)
        DataProvider.updateArtist(artist)
        Ok(JsObject(Seq("message" -> JsString("success"))))

      } catch {
        case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
      }

      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))

    }
  }

  def delete(id: Long) = Action { implicit request =>
    if (!AuthInfo.isAuthorized) Unauthorized(JsObject(Seq("error" -> JsString("unauthorized"))))
    else {
      DataProvider.deleteArtist(id)
      Ok(Json.obj("message" -> "success"))
    }
  }

  private def parseArtist(js: JsValue): Artist = Artist (
    name = (js \ "name").as[String],
    description = (js \ "description").as[String]
  )
}
