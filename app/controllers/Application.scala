package controllers

import play.api._
import play.api.mvc._
import org.pac4j.http.client._
import org.pac4j.core.profile._
import org.pac4j.play._
import org.pac4j.play.scala._
import play.api.libs.json.Json

object Application extends ScalaController {

  def index = Action { request =>
    val newSession = getOrCreateSessionId(request)
    val urlFacebook = getRedirectionUrl(request, newSession, "FacebookClient", "/?0")
    val urlTwitter = getRedirectionUrl(request, newSession, "TwitterClient", "/?1")
    val urlForm = getRedirectionUrl(request, newSession, "FormClient", "/?2")
    val urlBA = getRedirectionUrl(request, newSession, "BasicAuthClient", "/?3")
    val urlCas = getRedirectionUrl(request, newSession, "CasClient", "/?4")
    val urlGoogleOpenId = getRedirectionUrl(request, newSession, "GoogleOpenIdClient", "/?5")
    val profile = getUserProfile(request)
    Ok(views.html.index(profile, urlFacebook, urlTwitter, urlForm, urlBA, urlCas, urlGoogleOpenId)).withSession(newSession)
  }

  def facebookIndex = RequiresAuthentication("FacebookClient") { profile =>
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
  def formIndexJson = RequiresAuthentication("FormClient", "", true) { profile =>
    Action { request =>
      val content = views.html.protectedIndex.render(profile)
      val json = Json.obj("content" -> content.toString())
      Ok(json).as("application/json")
    }
  }

  def basicauthIndex = RequiresAuthentication("BasicAuthClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def casIndex = RequiresAuthentication("CasClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def googleOpenIdIndex = RequiresAuthentication("GoogleOpenIdClient") { profile =>
    Action { request =>
      Ok(views.html.protectedIndex(profile))
    }
  }

  def theForm = Action { request =>
    val formClient = Config.getClients().findClient("FormClient").asInstanceOf[FormClient]
    Ok(views.html.theForm.render(formClient.getCallbackUrl()))
  }
}
