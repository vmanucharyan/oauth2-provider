package controllers

import java.time.{LocalDateTime, Duration}
import data.DataProvider
import models.oauth2.OAuthApp
import oauth2._
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object OAuth extends Controller {
  def auth(clientId: String, redirectUri: String, state: String) = Action.async { implicit rs =>
    if (AuthInfo.isAuthorized) {
      DataProvider.getApplication(clientId) map {
        case Some(app) =>
          Ok(views.html.oauth_grant_access(OAuthApp("id", "secret", "userId"), clientId, redirectUri, state))

        case None => Ok(JsObject(Seq("error" ->  JsString("wrong client_id"))))
      }

    }
    else {
      val returnUri = routes.OAuth.auth(clientId, redirectUri, state).absoluteURL()
      Future(Redirect(routes.SignIn.signIn(Some(returnUri))))
    }
  }

  def grantAccess(clientId: String, redirectUrl: String, state: String) = Action { implicit rs =>
    val codeGenerator = new RandAuthCodeGenerator()
    val code = codeGenerator.generate()

    Logger.debug(s"grant_access: ${AuthInfo.userId}")

    AuthSessionKeeper.storeOAuthCode(code, clientId, AuthInfo.userId.get)

    Logger.debug(s"grant_access: code: $code")

    Redirect (
      url = redirectUrl,
      queryString = Map("code" -> Seq(code), "state" -> Seq(state))
    )
  }

  def tokenGet(code: String,
               clientId: String,
               clientSecret: String,
               redirectUri: String,
               grantType: String) = Action.async { implicit request =>

    Logger.debug(s"/token: code - $code")

    if (grantType != "authorization_code")
      Future(Redirect(redirectUri, Map("error" -> Seq("grant_type must be 'authorization_code'"))))
    else AuthSessionKeeper.retreiveOAuthCode(code) match {
      case Some((cacheClientId, cacheUserId)) =>
        Logger.debug(s"$clientId $cacheClientId")

        if (clientId equals cacheClientId) {
          val tokenGenerator = new AlphaNumericTokenGenerator()
          val tokenString = tokenGenerator.generateToken()
          val token = new AccessToken(cacheUserId, tokenString, Duration.ofMinutes(60))

          AuthSessionKeeper.storeToken(token)

          DataProvider.getApplication(clientId).map {
            case Some(app) =>
              if (app.id == clientId && app.secret == clientSecret)
                Ok(JsObject(Seq(
                  "token" -> JsString(tokenString),
                  "expires_in" -> JsNumber(Duration.between(LocalDateTime.now(), token.expiresIn).getSeconds),
                  "grant_type" -> JsString("bearer")
                )))

              else
                Ok(JsObject(Seq(
                  "error" -> JsString("client_id does not match")
                )))

            case None =>
              Ok(JsObject(Seq(
                "error" -> JsString("wrong client_id")
              )))
          }
        }
        else Future {
          Ok(JsObject(Seq(
            "error" -> JsString("invalid code")
          )))
        }

      case None => Future {
        Ok(JsObject(Seq(
          "error" -> JsString("invalid code0")
        )))
      }
    }
  }

  def token() = Action.async { implicit request =>
    val params = request.body.asFormUrlEncoded.get

    val code = params("code")(0)
    val clientSecret = params("client_secret")(0)
    val clientId = params("client_id")(0)
    val redirectUri = params("redirect_uri")(0)
    val grantType = params("grant_type")(0)

    if (grantType != "authorization_code")
      Future(Redirect(redirectUri, Map("error" -> Seq("grant_type must be 'authorization_code'"))))
    else AuthSessionKeeper.retreiveOAuthCode(code) match {
      case Some((cacheClientId, cacheUserId)) =>
        Logger.debug(s"$clientId $cacheClientId")

        if (clientId equals cacheClientId) {
          val tokenGenerator = new AlphaNumericTokenGenerator()
          val tokenString = tokenGenerator.generateToken()
          val token = new AccessToken(cacheUserId, tokenString, Duration.ofMinutes(60))

          AuthSessionKeeper.storeToken(token)

          DataProvider.getApplication(clientId).map {
            case Some(app) =>
              if (app.id == clientId && app.secret == clientSecret)
                Ok(JsObject(Seq(
                  "token" -> JsString(tokenString),
                  "expires_in" -> JsNumber(Duration.between(LocalDateTime.now(), token.expiresIn).getSeconds),
                  "grant_type" -> JsString("bearer")
                )))

              else
                Ok(JsObject(Seq(
                  "error" -> JsString("client_id does not match")
                )))

            case None =>
              Ok(JsObject(Seq(
                "error" -> JsString("wrong client_id")
              )))
          }
        }
        else Future {
          Ok(JsObject(Seq(
            "error" -> JsString("invalid code")
          )))
        }

      case None => Future {
        Ok(JsObject(Seq(
          "error" -> JsString("invalid code0")
        )))
      }
    }
  }
}
