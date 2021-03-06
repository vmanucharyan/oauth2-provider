package oauth2

import java.util.NoSuchElementException

import models.oauth2.AccessToken
import play.api.cache.Cache
import play.api.Play.current
import play.api.mvc.{AnyContent, Request}

object AuthInfo {
  def isAuthorized(implicit rs: Request[AnyContent]): Boolean =
    getUserToken() match {
      case Some(tokenValue) => getAccessToken(tokenValue) match {
        case Some(token) => !token.isExpired
        case None => false
      }
      case None => false
    }

  def isExpired(implicit rs: Request[AnyContent]): Boolean =
    getUserToken() match {
      case Some(tokenValue) => getAccessToken(tokenValue) match {
        case Some(token) => token.isExpired
        case None => throw new NoSuchElementException("token not found")
      }
      case None => false
    }

  def acessToken (implicit rs: Request[AnyContent]): Option[AccessToken] =
    getUserToken() match {
      case Some(userTokenString) => getAccessToken(userTokenString)
      case None => None
    }

  def userId(implicit rs: Request[AnyContent]): Option[String] =
    acessToken match {
      case Some(token) => Some(token.userId)
      case None => None
    }

  private def getUserToken() (implicit rs: Request[AnyContent]): Option[String] =
    checkSession() match {
      case Some(token) => Some(token)
      case None => checkHeader() match {
        case Some(headerToken) => Some(headerToken)
        case None => checkQuery()
      }
    }

  private def checkHeader() (implicit rs: Request[AnyContent]): Option[String] =
    rs.headers.get("Authorization") match {
      case Some(str) => str.split(' ') match {
        case Array(tokenType, tokenString) => Some(tokenString)
        case _ => None
      }
      case None => None
    }

  private def checkSession() (implicit rs: Request[AnyContent]): Option[String] =
    rs.session.get("token")

  private def checkQuery() (implicit rs: Request[AnyContent]): Option[String] =
    rs.queryString.get("token") match {
      case Some(sequence) => Some(sequence(0))
      case None => None
    }

  private def getAccessToken(userToken: String) (implicit rs: Request[AnyContent]): Option[AccessToken] =
    AuthSessionKeeper.retreiveToken(userToken)
}
