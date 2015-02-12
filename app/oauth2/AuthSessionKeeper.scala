package oauth2

import data.DataProvider
import models.oauth2.{RefreshToken, AccessToken}
import play.api.cache.Cache
import play.api.Play.current

object AuthSessionKeeper {
  def storeToken(token: AccessToken): Long =
    DataProvider.insertAccessToken(token)

  def retreiveToken(tokenValue: String): Option[AccessToken] =
    DataProvider.getAccessToken(tokenValue)

  def removeToken(token: AccessToken): Unit =
    DataProvider.deleteAccessToken(token.id)

  def storeRefreshToken(token: RefreshToken) : Long =
    DataProvider.insertRefreshToken(token)

  def retreiveRefreshToken(token: String) : Option[RefreshToken] =
    DataProvider.getRefreshToken(token)

  def storeOAuthCode(code: String, clientId: String, userId: String): Unit =
    Cache.set(code, (clientId, userId))

  def retreiveOAuthCode(code: String): Option[(String, String)] =
    Cache.getAs[(String, String)](code)
}
