package controllers

import org.pac4j.core.profile.{CommonProfile, ProfileManager, UserProfile}
import org.pac4j.core.util.CommonHelper
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.profile.JwtGenerator
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.Security
import play.api.libs.json.Json
import javax.inject.Inject

import play.libs.concurrent.HttpExecutionContext
import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._

import scala.collection.JavaConversions._

class ApplicationWithFilter @Inject() (val config: Config, val playSessionStore: PlaySessionStore, override val ec: HttpExecutionContext) extends Controller with Security[CommonProfile] {

  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }

  def index = Secure("AnonymousClient", "csrfToken") { profiles =>
    Action { request =>
      val webContext = new PlayWebContext(request, playSessionStore)
      val csrfToken = webContext.getSessionAttribute(Pac4jConstants.CSRF_TOKEN).asInstanceOf[String]
      Ok(views.html.index(profiles, csrfToken, null))
    }
  }

  def facebookIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def facebookNotProtectedIndex = Action { request =>
    Ok(views.html.notprotectedIndex(getProfiles(request)))
  }

  def facebookAdminIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def facebookCustomIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def twitterIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def protectedIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def protectedCustomIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def formIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
  // a 401 error response will be returned instead of a redirection to the login url.
  def formIndexJson = Action { implicit request =>
    val content = views.html.protectedIndex.render(getProfiles(request))
    val json = Json.obj("content" -> content.toString())
    Ok(json).as("application/json")
  }

  def basicauthIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def dbaIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def casIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def samlIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def oidcIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def restJwtIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def loginForm = Action { implicit request =>
    val formClient = config.getClients.findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.loginForm.render(formClient.getCallbackUrl))
  }

  def jwt = Action { implicit request =>
    val profiles = getProfiles(request)
    val generator = new JwtGenerator[CommonProfile](new SecretSignatureConfiguration("12345678901234567890123456789012"))
    var token: String = ""
    if (CommonHelper.isNotEmpty(profiles)) {
      token = generator.generate(profiles.get(0))
    }
    Ok(views.html.jwt.render(token))
  }
}
