package controllers

import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.profile.JwtGenerator
import play.api.mvc._
import org.pac4j.core.profile._
import org.pac4j.play.scala._
import play.api.libs.json.Json

class Application extends Controller with Security[CommonProfile] {

  def index = Action { request =>
    val profile = getUserProfile(request)
    Ok(views.html.index(profile))
  }

  def facebookIndex = RequiresAuthentication("FacebookClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def facebookAdminIndex = RequiresAuthentication("FacebookClient", "admin") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def facebookCustomIndex = RequiresAuthentication("FacebookClient", "custom") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def twitterIndex = RequiresAuthentication("TwitterClient,FacebookClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def protectedIndex = RequiresAuthentication { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def formIndex = RequiresAuthentication("FormClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
  // a 401 error response will be returned instead of a redirection to the login url.
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

  def dbaIndex = RequiresAuthentication("DirectBasicAuthClient,ParameterClient") { profile =>
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

  def loginForm = Action { request =>
    val formClient = config.getClients().findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.loginForm.render(formClient.getCallbackUrl()))
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
