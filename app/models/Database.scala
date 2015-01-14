package models

import play.api.db.slick._
import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend

object Database {
  private val users = TableQuery[UsersTable]

  def getUserById(id: String) (implicit session: JdbcBackend#Session) : Option[User] =
    users.filter(u => u.email === id).firstOption

  def getUsers() (implicit session: JdbcBackend#Session) : List[User] =
    users.list
}
