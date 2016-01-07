package controllers

import org.pac4j.core.profile.CommonProfile
import play.api.mvc._
import security.Security

class Application2 extends Controller with Security[CommonProfile] {

  def protectedIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile))
  }

}
