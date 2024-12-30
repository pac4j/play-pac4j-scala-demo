package controllers

import org.pac4j.cas.profile.CasProxyProfile
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.CallContext
import org.pac4j.core.exception.http.WithLocationAction
import org.pac4j.core.profile._
import org.pac4j.core.util.{CommonHelper, Pac4jConstants}
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import org.pac4j.play.PlayWebContext
import org.pac4j.play.context.PlayFrameworkParameters
import org.pac4j.play.scala._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.jdk.CollectionConverters._

class Application @Inject()(val controllerComponents: SecurityComponents, implicit val pac4jTemplateHelper: Pac4jScalaTemplateHelper[UserProfile]) extends Security[UserProfile] {

  private def getProfiles(implicit request: RequestHeader): List[UserProfile] = {
    val parameters = new PlayFrameworkParameters(request)
    val webContext = controllerComponents.config.getWebContextFactory.newContext(parameters)
    val sessionStore = controllerComponents.config.getSessionStoreFactory.newSessionStore(parameters)
    val profileManager = controllerComponents.config.getProfileManagerFactory.apply(webContext, sessionStore)
    val profiles = profileManager.getProfiles
    profiles.asScala.toList
  }

  def index: Action[AnyContent] = Secure("AnonymousClient") { implicit request =>
    //println(pac4jTemplateHelper.getCurrentProfile.get)
    val parameters = new PlayFrameworkParameters(request)
    val webContext = controllerComponents.config.getWebContextFactory.newContext(parameters).asInstanceOf[PlayWebContext]
    val sessionStore = controllerComponents.config.getSessionStoreFactory.newSessionStore(parameters)
    val sessionId = sessionStore.getSessionId(webContext, false).orElse("nosession")
    val csrfToken = webContext.getRequestAttribute(Pac4jConstants.CSRF_TOKEN).orElse(null).asInstanceOf[String]
    webContext.supplementResponse(Ok(views.html.index(profiles, csrfToken, sessionId)))
  }

  def csrfIndex: Action[AnyContent] = Secure("AnonymousClient", "csrfCheck") { implicit request =>
    Ok(views.html.csrf(profiles.asJava))
  }

  // secured by filter
  def facebookIndex: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def facebookAdminIndex: Action[AnyContent] = Secure("FacebookClient", "admin") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def facebookCustomIndex: Action[AnyContent] = Secure("FacebookClient", "custom") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def facebookNotProtectedIndex: Action[AnyContent] = Action { request =>
    Ok(views.html.notprotectedIndex(getProfiles(request)))
  }

  def twitterIndex: Action[AnyContent] = Secure("TwitterClient,FacebookClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def protectedIndex: Action[AnyContent] = Secure { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def protectedCustomIndex: Action[AnyContent] = Secure(authorizers = "custom") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def formIndex: Action[AnyContent] = Secure("FormClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
  // a 401 error response will be returned instead of a redirection to the login url.
  def formIndexJson: Action[AnyContent] = Secure("FormClient") { implicit request =>
    val content = views.html.protectedIndex.render(profiles, pac4jTemplateHelper, request)
    val json = Json.obj("content" -> content.toString())
    Ok(json).as("application/json")
  }

  def basicauthIndex: Action[AnyContent] = Secure("IndirectBasicAuthClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def dbaIndex: Action[AnyContent] = Secure("DirectBasicAuthClient,ParameterClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def casIndex: Action[AnyContent] = Secure("CasClient") { implicit request =>
    val profile = profiles.asJava.get(0)
    val service = "http://localhost:8080/proxiedService"
    var proxyTicket: String = null
    profile match
      case proxyProfile: CasProxyProfile =>
        proxyTicket = proxyProfile.getProxyTicketFor(service)
      case _ =>
    Ok(views.html.casProtectedIndex.render(profile, service, proxyTicket))
    //Ok(views.html.protectedIndex(profiles))
  }

  def samlIndex: Action[AnyContent] = Secure("SAML2Client") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  def oidcIndex: Action[AnyContent] = Secure("OidcClient") { implicit request =>
    Ok(views.html.protectedIndex(profiles))
  }

  // secured by filter
  def restJwtIndex: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.protectedIndex(getProfiles(request)))
  }

  def loginForm: Action[AnyContent] = Action { request =>
    val formClient = config.getClients.findClient("FormClient").get.asInstanceOf[FormClient]
    Ok(views.html.loginForm.render(formClient.getCallbackUrl))
  }

  def jwt: Action[AnyContent] = Action { request =>
    val profiles = getProfiles(request)
    val generator = new JwtGenerator(new SecretSignatureConfiguration("12345678901234567890123456789012"))
    var token: String = ""
    if (!CommonHelper.isEmpty(profiles.asJava)) {
      token = generator.generate(profiles.asJava.get(0))
    }
    Ok(views.html.jwt.render(token))
  }

  def forceLogin: Action[AnyContent] = Action { request =>
    val parameters = new PlayFrameworkParameters(request)
    val webContext = controllerComponents.config.getWebContextFactory.newContext(parameters).asInstanceOf[PlayWebContext]
    val sessionStore = controllerComponents.config.getSessionStoreFactory.newSessionStore(parameters)
    val client = config.getClients.findClient(webContext.getRequestParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER).get).get.asInstanceOf[IndirectClient]
    val location = client.getRedirectionAction(new CallContext(webContext, sessionStore, controllerComponents.config.getProfileManagerFactory)).get.asInstanceOf[WithLocationAction].getLocation
    webContext.supplementResponse(Redirect(location))
  }
}
