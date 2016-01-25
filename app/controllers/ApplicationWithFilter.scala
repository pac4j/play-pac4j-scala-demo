package controllers

import org.pac4j.core.profile.{UserProfile, CommonProfile}
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.profile.JwtGenerator
import org.pac4j.play.scala.Security
import play.api.libs.json.Json
import play.api.mvc._

class ApplicationWithFilter extends Controller with Security[CommonProfile] {

  def index = Action { implicit request =>
    val profile = getUserProfile.orNull
    Ok(views.html.index(profile))
  }

  def facebookIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def facebookAdminIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def facebookCustomIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def twitterIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def protectedIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def protectedCustomIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def formIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
  // a 401 error response will be returned instead of a redirection to the login url.
  def formIndexJson = Action { implicit request =>
    val content = views.html.protectedIndex.render(getUserProfile.get)
    val json = Json.obj("content" -> content.toString())
    Ok(json).as("application/json")
  }

  def basicauthIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def dbaIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def casIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def samlIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def oidcIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def restJwtIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getUserProfile.get))
  }

  def loginForm = Action { implicit request =>
    val formClient = config.getClients().findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.loginForm.render(formClient.getCallbackUrl()))
  }

  def jwt = Action { implicit request =>
    val profile = getUserProfile(request).orNull
    val generator = new JwtGenerator[UserProfile]("12345678901234567890123456789012")
    var token: String = ""
    if (profile != null) {
      token = generator.generate(profile)
    }
    Ok(views.html.jwt.render(token))
  }
}
