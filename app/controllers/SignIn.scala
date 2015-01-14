package controllers

import models.UsersTable
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick._
import play.api.mvc._

import scala.slick.driver.H2Driver.simple._

object SignIn extends Controller {
  val users = TableQuery[UsersTable]

  def signIn = Action {
    Ok(views.html.login())
  }

  def signInForm = Form (
    tuple (
      "email" -> nonEmptyText(),
      "password" -> nonEmptyText()
    )
  )

  def performSignIn = DBAction { implicit rs =>
    val (m, pwd) = signInForm.bindFromRequest.get

    users.filter(u => u.email === m).firstOption match {
      case Some(user) => Ok(views.html.index())
      case None => BadRequest("Пользователь не существует или заданный пароль неверен")
    }
  }
}
