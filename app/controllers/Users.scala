package controllers

import play.api.db.slick._
import play.api.mvc._
import scala.slick.driver.H2Driver.simple._

import models.UsersTable

object Users extends Controller {
  val users = TableQuery[UsersTable]

  def all = DBAction { implicit rs =>
    Ok(views.html.users(users.list))
  }
}
