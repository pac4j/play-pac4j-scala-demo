package controllers

import org.pac4j.core.client.IndirectClient
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.profile.JwtGenerator
import play.api.mvc._
import org.pac4j.core.profile._
import org.pac4j.core.util.CommonHelper
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala._
import play.api.libs.json.Json
import org.pac4j.core.credentials.Credentials
import javax.inject.Inject
import org.pac4j.cas.profile.CasProxyProfile
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.context.session.SessionStore
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration

import scala.collection.JavaConverters._

class Application @Inject() (val controllerComponents: SecurityComponents) extends Security[CommonProfile] {

  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }

  def index = Secure("AnonymousClient", "csrfToken") { implicit request =>
    val webContext = new PlayWebContext(request, playSessionStore)
    val sessionStore = webContext.getSessionStore().asInstanceOf[SessionStore[PlayWebContext]]
    val sessionId = sessionStore.getOrCreateSessionId(webContext)
    val csrfToken = sessionStore.get(webContext, Pac4jConstants.CSRF_TOKEN).asInstanceOf[String]
    Ok(views.html.index(profiles, csrfToken, sessionId))
  }

  def csrfIndex = Secure("AnonymousClient", "csrfCheck") { implicit request =>
    Ok(views.html.csrf(profiles.asJava))
  }

  // secured by filter
  def facebookIndex = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def facebookAdminIndex = Secure("FacebookClient", "admin") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def facebookCustomIndex = Secure("FacebookClient", "custom") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def facebookNotProtectedIndex = Action { request =>
    Ok(views.html.notprotectedIndex(getProfiles(request)))
  }

  def twitterIndex = Secure("TwitterClient,FacebookClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def protectedIndex = Secure { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def protectedCustomIndex = Secure(authorizers="custom") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def formIndex = Secure("FormClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
  // a 401 error response will be returned instead of a redirection to the login url.
  def formIndexJson = Secure("FormClient") { implicit request =>
    val content = views.html.protectedIndex.render(profiles)
    val json = Json.obj("content" -> content.toString())
    Ok(json).as("application/json")
  }

  def basicauthIndex = Secure("IndirectBasicAuthClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def dbaIndex = Secure("DirectBasicAuthClient,ParameterClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def casIndex = Secure("CasClient") { implicit request =>
    val profile = profiles.asJava.get(0)
    val service = "http://localhost:8080/proxiedService"
    var proxyTicket: String = null
    if (profile.isInstanceOf[CasProxyProfile]) {
      val proxyProfile = profile.asInstanceOf[CasProxyProfile]
      proxyTicket = proxyProfile.getProxyTicketFor(service)
    }
    Ok(views.html.casProtectedIndex.render(profile, service, proxyTicket))
    //Ok(views.html.protectedIndex(profiles))
  }
  
  def samlIndex = Secure("SAML2Client") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def oidcIndex = Secure("OidcClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  // secured by filter
  def restJwtIndex = Action { request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def loginForm = Action { request =>
    val formClient = config.getClients.findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.loginForm.render(formClient.getCallbackUrl))
  }

  def jwt = Action { request =>
    val profiles = getProfiles(request)
    val generator = new JwtGenerator[CommonProfile](new SecretSignatureConfiguration("12345678901234567890123456789012"))
    var token: String = ""
    if (CommonHelper.isNotEmpty(profiles.asJava)) {
      token = generator.generate(profiles.asJava.get(0))
    }
    Ok(views.html.jwt.render(token))
  }

  def forceLogin = Action { request =>
    val context: PlayWebContext = new PlayWebContext(request, playSessionStore)
    val client = config.getClients.findClient(context.getRequestParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)).asInstanceOf[IndirectClient[Credentials,CommonProfile]]
    val location = client.getRedirectAction(context).getLocation
    val newSession = new Session(mapAsScalaMap(context.getJavaSession).toMap)
    Redirect(location).withSession(newSession)
  }
}
