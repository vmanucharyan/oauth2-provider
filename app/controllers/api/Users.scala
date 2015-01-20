package controllers.api

import data.DataProvider
import oauth2.AuthInfo
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Users extends Controller {
  def me() = Action.async { implicit request =>
    AuthInfo.userId match {
      case Some(userId) =>
        DataProvider.getUserById(userId) map {
          case Some(user) => Ok(JsObject(Seq(
            "email" -> JsString(user.email),
            "full_name" -> JsString(user.fullName)
          )))

          case None => InternalServerError("user not found in database")
        }

      case None => Future(Unauthorized("unauthorized"))
    }
  }
}
