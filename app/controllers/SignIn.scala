package controllers

import java.time.{LocalTime, Duration}

import _root_.oauth2.{AccessToken, AlphaNumericTokenGenerator}
import models.{DatabaseAccess, UsersHelper}
import play.api.cache.Cache
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

object SignIn extends Controller {
  def signIn(redirect: Option[String]) = Action {
    Ok(views.html.login())
  }

  def signInForm = Form (
    tuple (
      "email" -> nonEmptyText(),
      "password" -> nonEmptyText()
    )
  )

  def performSignIn(redirect: Option[String]) = Action.async { implicit rs =>
    val (m, pwd) = signInForm.bindFromRequest.get

    val tokenGenerator = new AlphaNumericTokenGenerator()

    DatabaseAccess.getUserById(m).map {
      case Some(user) =>
        val pwdHash = UsersHelper.hashPassword(pwd)
        if (pwdHash == user.passHash) {

          val token = new AccessToken(m, tokenGenerator.generateToken(), Duration.ofMinutes(5))

          val session = {
            "uid" -> m
            "token" -> token.value
          }

          Cache.set(token.value, token)

          redirect match {
            case Some(url) => Redirect(url).withSession(session)
            case None => Ok(views.html.index()).withSession(session)
          }
        }
        else BadRequest(views.html.static_pages.nosuchuser())

      case None => BadRequest(views.html.static_pages.nosuchuser())
    }
  }
}
