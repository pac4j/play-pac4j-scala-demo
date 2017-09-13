package controllers

import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.core.util.CommonHelper
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.profile.JwtGenerator
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.Security
import play.api.libs.json.Json
import javax.inject.Inject

import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._

import scala.collection.JavaConverters._

class ApplicationWithFilter @Inject() (val controllerComponents: ControllerComponents, val config: Config, val playSessionStore: PlaySessionStore, val actionBuilder: DefaultActionBuilder) extends Security[CommonProfile] {

  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }

  def index = Secure("AnonymousClient", "csrfToken") { profiles =>
    actionBuilder { request =>
      val webContext = new PlayWebContext(request, playSessionStore)
      val csrfToken = webContext.getSessionAttribute(Pac4jConstants.CSRF_TOKEN).asInstanceOf[String]
      Ok(views.html.index(profiles, csrfToken, null))
    }
  }

  def facebookIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def facebookAdminIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def facebookCustomIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def twitterIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def protectedIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def protectedCustomIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def formIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
  // a 401 error response will be returned instead of a redirection to the login url.
  def formIndexJson = actionBuilder { implicit request =>
    val content = views.html.protectedIndex.render(getProfiles(request))
    val json = Json.obj("content" -> content.toString())
    Ok(json).as("application/json")
  }

  def basicauthIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def dbaIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def casIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def samlIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def oidcIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def restJwtIndex = actionBuilder { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def loginForm = actionBuilder { implicit request =>
    val formClient = config.getClients.findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.loginForm.render(formClient.getCallbackUrl))
  }

  def jwt = actionBuilder { implicit request =>
    val profiles = getProfiles(request)
    val generator = new JwtGenerator[CommonProfile](new SecretSignatureConfiguration("12345678901234567890123456789012"))
    var token: String = ""
    if (CommonHelper.isNotEmpty(profiles.asJava)) {
      token = generator.generate(profiles.asJava.get(0))
    }
    Ok(views.html.jwt.render(token))
  }
}
