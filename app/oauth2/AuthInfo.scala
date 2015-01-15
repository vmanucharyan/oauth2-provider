package oauth2

import play.Logger
import play.api.cache.Cache
import play.api.Play.current
import play.api.mvc.{Session, AnyContent, Request}

object AuthInfo {
  def isAuthorized(implicit rs: Request[AnyContent]): Boolean = {
    val authHeader = rs.headers.get("Authorization")

    Logger.debug(s"Header: $authHeader")
    Logger.debug(s"session token: ${rs.session.get("token")}")

    val userToken =
      rs.headers.get("Authorization") match {
        case Some(str) => Some(str)
        case None => rs.session.get("token")
      }

    userToken match {
      case Some(tokenValue) =>
        val cacheToken = Cache.getAs[AccessToken](tokenValue)
        Logger.debug(s"Cache token $cacheToken")

        cacheToken match {
          case Some(token) => true
          case None => false
        }

      case None => false
    }
  }
}
