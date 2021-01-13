package controllers

import javax.inject.Inject
import org.pac4j.core.profile._
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.play.scala._

class ApplicationWithScalaHelper @Inject()(implicit val pac4jTemplateHelper: Pac4jScalaTemplateHelper[CommonProfile],val controllerComponents: SecurityComponents) extends Security[CommonProfile] {



  def index = Secure("AnonymousClient", "csrfToken") { implicit request =>
    if(pac4jTemplateHelper.getCurrentProfile.isDefined) {
      Redirect(routes.ApplicationWithScalaHelper.userView())
    } else {
      Redirect(routes.ApplicationWithScalaHelper.loginForm())
    }
  }


  def userView = Secure("FormClient") { implicit request =>
    Ok(views.html.scalaHelper.user())
  }


  def loginForm = Action { implicit request =>
    val formClient = config.getClients.findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.scalaHelper.loginForm(formClient.getCallbackUrl))
  }

}
