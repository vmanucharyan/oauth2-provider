package models

import play.api.db.slick._
import scala.concurrent.{ExecutionContext, Future}
import scala.slick.driver.H2Driver.simple._
import play.api.Play.current

object DatabaseAccess {
  private val users = TableQuery[UsersTable]

  def getUserById(id: String)(implicit context: ExecutionContext): Future[Option[User]] = Future {
    DB.withSession(implicit session => {
      users.filter(u => u.email === id).firstOption
    })
  }

  def getUsers()(implicit context: ExecutionContext): Future[List[User]] = Future {
    DB.withSession(implicit s => {
      users.list
    })
  }
}
