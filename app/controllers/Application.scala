package controllers

import org.pac4j.cas.client.CasClient
import org.pac4j.http.client.indirect.{IndirectBasicAuthClient, FormClient}
import org.pac4j.jwt.profile.JwtGenerator
import org.pac4j.oauth.client.{TwitterClient, FacebookClient}
import org.pac4j.oidc.client.OidcClient
import org.pac4j.saml.client.SAML2Client
import play.api.mvc._
import org.pac4j.core.profile._
import org.pac4j.play._
import org.pac4j.play.scala._
import play.api.libs.json.Json

class Application extends Controller with Security[CommonProfile] {

  def index = Action { request =>
    val newSession = getOrCreateSessionId(request)
    val webContext = new PlayWebContext(request, dataStore)
    val clients = config.getClients()
    val urlFacebook = (clients.findClient("FacebookClient").asInstanceOf[FacebookClient]).getRedirectAction(webContext, false, false).getLocation;
    val urlTwitter = (clients.findClient("TwitterClient").asInstanceOf[TwitterClient]).getRedirectAction(webContext, false, false).getLocation;
    val urlForm = (clients.findClient("FormClient").asInstanceOf[FormClient]).getRedirectAction(webContext, false, false).getLocation;
    val urlBA = (clients.findClient("IndirectBasicAuthClient").asInstanceOf[IndirectBasicAuthClient]).getRedirectAction(webContext, false, false).getLocation;
    val urlCas = (clients.findClient("CasClient").asInstanceOf[CasClient]).getRedirectAction(webContext, false, false).getLocation;
    val urlOidc = "" //(clients.findClient("OidcClient").asInstanceOf[OidcClient]).getRedirectAction(webContext, false, false).getLocation;
    val urlSaml = "" //(clients.findClient("SAML2Client").asInstanceOf[SAML2Client]).getRedirectAction(webContext, false, false).getLocation;
    val profile = getUserProfile(request)
    Ok(views.html.index(profile, urlFacebook, urlTwitter, urlForm, urlBA, urlCas, urlOidc, urlSaml)).withSession(newSession)
  }

  def facebookIndex = RequiresAuthentication("FacebookClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def facebookAdminIndex = RequiresAuthentication("FacebookClient", "ROLE_ADMIN", null) { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def facebookCustomIndex = RequiresAuthentication("FacebookClient", new CustomAuthorizer()) { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def twitterIndex = RequiresAuthentication("TwitterClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def formIndex = RequiresAuthentication("FormClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  // Setting the isAjax parameter to true will result in a 401 error response
  // instead of redirecting to the login url.
  def formIndexJson = RequiresAuthentication("FormClient") { profile =>
    Action { request =>
      val content = views.html.protectedIndex.render(profile)
      val json = Json.obj("content" -> content.toString())
      Ok(json).as("application/json")
    }
  }

  def basicauthIndex = RequiresAuthentication("IndirectBasicAuthClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def casIndex = RequiresAuthentication("CasClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }
  
  def samlIndex = RequiresAuthentication("SAML2Client") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def oidcIndex = RequiresAuthentication("OidcClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def restJwtIndex = RequiresAuthentication("ParameterClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def theForm = Action { request =>
    val formClient = config.getClients().findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.theForm.render(formClient.getCallbackUrl()))
  }

  def jwt = Action { request =>
    val profile = getUserProfile(request)
    val generator = new JwtGenerator[UserProfile]("12345678901234567890123456789012")
    var token: String = ""
    if (profile != null) {
      token = generator.generate(profile)
    }
    Ok(views.html.jwt.render(token))
  }
}
