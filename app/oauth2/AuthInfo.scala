package oauth2

import play.api.cache.Cache
import play.api.Play.current
import play.api.mvc.{AnyContent, Request}

object AuthInfo {
  def isAuthorized(implicit rs: Request[AnyContent]): Boolean =
    getUserToken() match {
      case Some(tokenValue) => getAccessToken(tokenValue).isDefined
      case None => false
    }

  def acessToken (implicit rs: Request[AnyContent]): Option[AccessToken] =
    getUserToken() match {
      case Some(userTokenString) => getAccessToken(userTokenString)
      case None => None
    }

  private def getUserToken() (implicit rs: Request[AnyContent]): Option[String] =
    checkHeader() match {
      case Some(token) => Some(token)
      case None => checkSession()
    }

  private def checkHeader() (implicit rs: Request[AnyContent]): Option[String] =
    rs.headers.get("Authorization")

  private def checkSession() (implicit rs: Request[AnyContent]): Option[String] =
    rs.session.get("token")

  private def getAccessToken(userToken: String) (implicit rs: Request[AnyContent]): Option[AccessToken] =
    Cache.getAs[AccessToken](userToken)
}
