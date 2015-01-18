package controllers

import java.time.Duration

import models.DataProvider
import models.oauth2.OAuthApp
import oauth2._
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

    if (grantType != "bearer")
      Future(Redirect(redirectUri, Map("error" -> Seq("grant_type must be 'bearer'"))))

    Cache.getAs[String](code) match {
      case Some(cacheClientId) =>
        Logger.debug(s"$clientId $cacheClientId")

        if (clientId equals cacheClientId) {
          val tokenGenerator = new AlphaNumericTokenGenerator()
          val tokenString = tokenGenerator.generateToken()
          val token = new AccessToken(clientId, tokenString, Duration.ofMinutes(5))

          AuthSessionKeeper.storeToken(token)

          DataProvider.getApplication(clientId).map {
            case Some(app) =>
              if (app.id == token.userId && app.secret == clientSecret)
                Redirect(redirectUri, Map("token" -> Seq(tokenString)))
              else
                Redirect(redirectUri, Map("error" -> Seq("app id does not match")))

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
