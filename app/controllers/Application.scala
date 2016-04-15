package controllers

import org.pac4j.core.client.{Clients, IndirectClient}
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.jwt.profile.JwtGenerator
import play.api.mvc._
import org.pac4j.core.profile._
import org.pac4j.core.util.CommonHelper
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala._
import play.api.libs.json.Json
import org.pac4j.core.credentials.Credentials

import scala.collection.JavaConversions._

class Application extends Controller with Security[CommonProfile] {

  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, config.getSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }

  def index = Action { request =>
    val profiles = getProfiles(request)
    Ok(views.html.index(profiles))
  }

  def facebookIndex = Secure("FacebookClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def facebookAdminIndex = Secure("FacebookClient", "admin") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def facebookCustomIndex = Secure("FacebookClient", "custom") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def twitterIndex = Secure("TwitterClient,FacebookClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def protectedIndex = Secure { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def formIndex = Secure("FormClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  // Setting the isAjax parameter is no longer necessary as AJAX requests are automatically detected:
  // a 401 error response will be returned instead of a redirection to the login url.
  def formIndexJson = Secure("FormClient") { profiles =>
    Action { request =>
      val content = views.html.protectedIndex.render(profiles)
      val json = Json.obj("content" -> content.toString())
      Ok(json).as("application/json")
    }
  }

  def basicauthIndex = Secure("IndirectBasicAuthClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def dbaIndex = Secure("DirectBasicAuthClient,ParameterClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def casIndex = Secure("CasClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }
  
  def samlIndex = Secure("SAML2Client") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def oidcIndex = Secure("OidcClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def restJwtIndex = Secure("ParameterClient") { profiles =>
    Action { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def loginForm = Action { request =>
    val formClient = config.getClients().findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.loginForm.render(formClient.getCallbackUrl()))
  }

  def jwt = Action { request =>
    val profiles = getProfiles(request)
    val generator = new JwtGenerator[CommonProfile]("12345678901234567890123456789012")
    var token: String = ""
    if (CommonHelper.isNotEmpty(profiles)) {
      token = generator.generate(profiles.get(0))
    }
    Ok(views.html.jwt.render(token))
  }

  def forceLogin = Action { request =>
    val context: PlayWebContext = new PlayWebContext(request, config.getSessionStore)
    val client = config.getClients.findClient(context.getRequestParameter(Clients.DEFAULT_CLIENT_NAME_PARAMETER)).asInstanceOf[IndirectClient[Credentials,CommonProfile]]
    Redirect(client.getRedirectAction(context).getLocation)
  }
}
