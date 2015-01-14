package controllers

import models._

import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._
import play.api.mvc._

import scala.slick.driver.H2Driver.simple._

object Register extends Controller {
  val users = TableQuery[UsersTable]

  def register = Action {
    Ok(views.html.register())
  }

  def registerForm = Form (
    tuple (
      "email" -> nonEmptyText(3, 255),
      "full_name" -> nonEmptyText(1, 255),
      "password" -> nonEmptyText(3, 20)
    )
  )

  def performRegister = DBAction { implicit rs =>
    val (email, fullName, pass) = registerForm.bindFromRequest.get
    val passHash = UsersHelper.hashPassword(pass)
    val user = User(email, fullName, passHash)

    users.insert(user)

    Redirect(routes.Application.index)
  }
}
