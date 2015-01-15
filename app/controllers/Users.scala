package controllers

import _root_.oauth2.AuthInfo
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Users extends Controller {
  def all = Action.async { implicit rs =>
    if (AuthInfo.isAuthorized) {
      models.DatabaseAccess.getUsers()
        .map(users => Ok(views.html.users(users)))
    }
    else Future(Unauthorized("Unauthorized"))
  }
}
