package models.oauth2

import play.api.db.slick.Config.driver.simple._

import scala.slick.ast.ColumnOption.Length

import models.UsersTable

case class OAuthApp(id: String, secret: String, userId: String)

class OAuthAppsTable(tag: Tag) extends Table[OAuthApp](tag, "OAUTH_APPS") {
  def id = column[String]("id", O.PrimaryKey, Length(40, varying = false))
  def secret = column[String]("secret", O.NotNull, Length(255, varying = false))
  def userId = column[String]("user_id", O.NotNull)

  def user = foreignKey("fk_user_id", userId, TableQuery[UsersTable])(u => u.email)

  def * = (id, secret, userId) <> (OAuthApp.tupled, OAuthApp.unapply)
}
