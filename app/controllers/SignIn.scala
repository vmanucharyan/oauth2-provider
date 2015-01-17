package controllers

import java.time.{LocalTime, Duration}

import oauth2.{AuthSessionKeeper, AccessToken, AlphaNumericTokenGenerator}
import models.{DatabaseAccess, UsersHelper}
import play.Logger
import play.api.cache.Cache
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

object SignIn extends Controller {
  def signIn(redirect: Option[String]) = Action {
    Logger.debug(s"signin: $redirect")
    Ok(views.html.login(redirect))
  }

  def signInForm = Form (
    tuple (
      "email" -> nonEmptyText(),
      "password" -> nonEmptyText()
    )
  )

  def performSignIn(redirectUri: Option[String]) = Action.async { implicit rs =>
    val (m, pwd) = signInForm.bindFromRequest.get

    Logger.debug(s"$redirectUri")

    val tokenGenerator = new AlphaNumericTokenGenerator()

    DatabaseAccess.getUserById(m).map {
      case Some(user) =>
        val pwdHash = UsersHelper.hashPassword(pwd)
        if (pwdHash == user.passHash) {

          val token = new AccessToken(m, tokenGenerator.generateToken(), Duration.ofMinutes(5))
          AuthSessionKeeper.storeToken(token)

          Logger.debug(s"$redirectUri")

          redirectUri match {
            case Some(url) => Redirect(url).withSession("token" -> token.value)
            case None => Ok(views.html.index()).withSession("token" -> token.value)
          }
        }
        else BadRequest(views.html.static_pages.nosuchuser())

      case None => BadRequest(views.html.static_pages.nosuchuser())
    }
  }
}
