package models.oauth2

import org.joda.time.{Duration, LocalDateTime}
import play.api.db.slick.Config.driver.simple._
import com.github.tototoshi.slick.H2JodaSupport._

case class AccessToken(userId: String,
                       value: String,
                       expiresIn: LocalDateTime,
                       tokenType: String,
                       refreshTokenId: Long,
                       id: Long = 0 ) {
  def isExpired = LocalDateTime.now().isAfter(expiresIn)
}

class AccessTokenTable(tag: Tag) extends Table[AccessToken](tag, "ACCESS_TOKEN") {
  def value = column[String]("value", O.NotNull)
  def userId = column[String]("user_id", O.NotNull)
  def expiresIn = column[LocalDateTime]("expires_in", O.NotNull)
  def tokenType = column[String]("token_type", O.NotNull)
  def refreshTokenId = column[Long]("refresh_token_id", O.NotNull)
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def uniqueValue = index("accestoken_value_idx_unique", value, unique = true)

  def * = (userId, value, expiresIn, tokenType, refreshTokenId, id) <> (AccessToken.tupled, AccessToken.unapply)
}
