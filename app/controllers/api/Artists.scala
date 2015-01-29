package controllers.api

import data.DataProvider
import models.{Artist, Album}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Artists extends Controller {
  def all() = Action.async {
    DataProvider.getAllArtists() map { artists =>
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

  def id(id: Int) = Action.async {
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

  def insertArtist() = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        try {
          val artist = Artist(
            name = (json \ "name").as[String],
            description = (json \ "description").as[String]
          )
          DataProvider.insertArtist(artist)
          Ok(JsObject(Seq("message" -> JsString("success"))))
        }
        catch {
          case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
        }

      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))
    }
  }
}
