package models.oauth2

import play.api.db.slick.Config.driver.simple._

case class RefreshToken(value: String, id: Long = 0)

class RefreshTokenTable(tag: Tag) extends Table[RefreshToken](tag, "REFRESH_TOKEN") {
  def value = column[String]("value", O.NotNull)
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def valueUnique = index("refreshtoken_value_idx_unique", value, unique = true)

  def * = (value, id) <> (RefreshToken.tupled, RefreshToken.unapply)
}
