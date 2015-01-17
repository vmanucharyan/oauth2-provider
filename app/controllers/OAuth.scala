package controllers

import java.time.Duration

import models.DatabaseAccess
import models.oauth2.OAuthApp
import oauth2.{AccessToken, AlphaNumericTokenGenerator, RandAuthCodeGenerator, AuthInfo}
import play.Logger
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object OAuth extends Controller {
  def auth(clientId: String, redirectUri: String) = Action { implicit rs =>
    if (AuthInfo.isAuthorized) {
      Ok(views.html.oauth_grant_access(OAuthApp("id", "secret", "userId"), clientId, redirectUri))
    }
    else {
      val returnUri = routes.OAuth.auth(clientId, redirectUri).absoluteURL()
      Redirect(routes.SignIn.signIn(Some(returnUri)))
    }
  }

  def grantAccess(clientId: String, redirectUrl: String) = Action { implicit rs =>
    val codeGenerator = new RandAuthCodeGenerator()
    Logger.debug(redirectUrl)
    val code = codeGenerator.generate()

    Cache.set(code, clientId)
    Logger.debug(s"grant_access: code: $code")

    Redirect (
      url = redirectUrl,
      queryString = Map("code" -> Seq(code))
    )
  }

  def token(code: String,
            clientId: String,
            clientSecret: String,
            redirectUri: String,
            grantType: String) = Action.async { implicit request =>

    Logger.debug(s"/token: code - $code")
    Cache.getAs[String](code) match {
      case Some(cacheClientId) =>
        Logger.debug(s"$clientId $cacheClientId")
        if (clientId equals cacheClientId) {val tokenGenerator = new AlphaNumericTokenGenerator()
          val tokenString = tokenGenerator.generateToken()
          val token = new AccessToken(clientId, tokenString, Duration.ofMinutes(5))

          DatabaseAccess.getApplication(clientId).map {
            case Some(app) => Redirect(redirectUri, Map("token" -> Seq(tokenString)))
            case None => Redirect(redirectUri, Map("error" -> Seq("app not found")))
          }
        }
        else Future {
          Redirect(redirectUri, Map("error" -> Seq("invalid code")))
        }

      case None => Future { Redirect(redirectUri, Map("error" -> Seq("invalid code0"))) }
    }
  }
}
