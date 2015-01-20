package controllers

import data.TestData
import play.api.mvc._

object Application extends Controller {
  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def fill = Action { implicit request =>
    TestData.fill()
    Redirect(routes.Application.index())
  }
}
