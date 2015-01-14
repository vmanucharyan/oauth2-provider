package controllers

import play.api.db.slick._
import play.api.mvc._

object Users extends Controller {
  def all = DBAction { implicit rs =>
    Ok(views.html.users(models.Database.getUsers))
  }
}
