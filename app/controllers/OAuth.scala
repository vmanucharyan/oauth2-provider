package controllers

import org.joda.time._
import data.DataProvider
import models.oauth2.{RefreshToken, AccessToken, OAuthApp}
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

  def token() = Action.async { implicit request =>
    val params = request.body.asFormUrlEncoded.get

    val code = params("code")(0)
    val clientSecret = params("client_secret")(0)
    val clientId = params("client_id")(0)
    val redirectUri = params("redirect_uri")(0)
    val grantType = params("grant_type")(0)

    if (grantType != "authorization_code")
      Future(BadRequest(Json.obj("error" -> "ivalid_grant")))
    else AuthSessionKeeper.retreiveOAuthCode(code) match {

      case Some((cacheClientId, cacheUserId)) =>

        Logger.debug(s"$clientId $cacheClientId")

        if (clientId equals cacheClientId) {
          val tokenGenerator = new AlphaNumericTokenGenerator()
          val tokenString = tokenGenerator.generateToken()

          val refreshToken = RefreshToken(tokenGenerator.generateToken())
          val refreshTokenId = AuthSessionKeeper.storeRefreshToken(refreshToken)

          val token = new AccessToken(cacheUserId, tokenString, LocalDateTime.now().plusMinutes(2), "bearer", refreshTokenId)

          AuthSessionKeeper.storeToken(token)

          DataProvider.getApplication(clientId).map {
            case Some(app) =>
              if (app.id == clientId && app.secret == clientSecret)

                Ok(Json.obj(
                  "token" -> tokenString,
                  "expires_in" -> JsNumber(Seconds.secondsBetween(LocalDateTime.now(), token.expiresIn).getSeconds),
                  "grant_type" -> "bearer",
                  "refresh_token" -> refreshToken.value
                ))

              else BadRequest(Json.obj("error" -> "invalid_client"))

            case None => BadRequest(Json.obj("error" -> "invalid_client"))
          }
        }

        else Future(BadRequest(Json.obj("error" -> "invalid_grant")))

      case None => Future(BadRequest(Json.obj("error" -> "invalid code0")))
    }
  }

  def refresh() = Action { implicit request =>
    val params = request.body.asFormUrlEncoded.get

    val refreshToken = params("refresh_token")(0)
    val grantType = params("grant_type")(0)

    if (grantType != "refresh_token") BadRequest(Json.obj("error" -> "unsupported_grant_type"))
    else {
      AuthSessionKeeper.retreiveRefreshToken(refreshToken) match {
        case Some(token) =>
          val accessToken = DataProvider.getAccessTokenOfRefreshToken(token.id)

          val tokenGenerator = new AlphaNumericTokenGenerator()
          val tokenString = tokenGenerator.generateToken()

          val newToken = new AccessToken(
            accessToken.userId,
            tokenString,
            LocalDateTime.now().plusMinutes(60),
            "bearer",
            token.id
          )

          AuthSessionKeeper.storeToken(newToken)
          AuthSessionKeeper.removeToken(accessToken)

          Ok(Json.obj(
            "token" -> tokenString,
            "expires_in" -> Seconds.secondsBetween(LocalDateTime.now(), newToken.expiresIn).getSeconds,
            "grant_type" -> "bearer",
            "refresh_token" -> token.value
          ))

        case None => BadRequest(Json.obj("error" -> "invalid_grant"))
      }
    }
  }
}
