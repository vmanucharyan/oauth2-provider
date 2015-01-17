package oauth2

import play.api.cache.Cache
import play.api.Play.current

object AuthSessionKeeper {
  def storeToken(token: AccessToken): Unit =
    Cache.set(token.value, token)

  def retreiveToken(tokenValue: String): Option[AccessToken] =
    Cache.getAs[AccessToken](tokenValue)

  def removeToken(token: AccessToken): Unit =
    Cache.remove(token.value)
}
