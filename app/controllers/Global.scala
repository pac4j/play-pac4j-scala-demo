package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import org.pac4j.play._
import org.pac4j.core.client._
import org.pac4j.cas.client._
import org.pac4j.saml.client._
import org.pac4j.oauth.client._
import org.pac4j.http.client._
import org.pac4j.openid.client._
import org.pac4j.http.credentials._
import play.api.mvc.Results._

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, t: Throwable): Result = {
    InternalServerError(
      views.html.error500.render())
  }

  override def onStart(app: Application) {
    Config.setErrorPage401(views.html.error401.render().toString())
    Config.setErrorPage403(views.html.error403.render().toString())

    // OAuth
    val facebookClient = new FacebookClient("132736803558924", "e461422527aeedb32ee6c10834d3e19e")
    val twitterClient = new TwitterClient("HVSQGAw2XmiwcKOTvZFbQ", "FSiO9G9VRR4KCuksky0kgGuo8gAVndYymr4Nl7qc8AA")

    // HTTP
    val formClient = new FormClient("http://localhost:9000/theForm", new SimpleTestUsernamePasswordAuthenticator())
    val basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())

    // CAS
    val casClient = new CasClient()
    //casClient.setGateway(true)
    //casClient.setLogoutHandler(new PlayLogoutHandler())
    casClient.setCasLoginUrl("https://freeuse1.casinthecloud.com/leleujgithub/login")
    casClient.setCasPrefixUrl("https://freeuse1.casinthecloud.com/leleujgithub/p3")

    // SAML
    val saml2Client = new Saml2Client()
    saml2Client.setKeystorePath(Play.application.resource("samlKeystore.jks").get.getFile())
    saml2Client.setKeystorePassword("pac4j-demo-passwd")
    saml2Client.setPrivateKeyPassword("pac4j-demo-passwd")
    saml2Client.setIdpMetadataPath(Play.application.resource("openidp-feide.xml").get.getFile())

    val clients = new Clients("http://localhost:9000/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient, saml2Client)
    Config.setClients(clients)
    // for test purposes : profile timeout = 60 seconds
    // Config.setProfileTimeout(60)
  }
}
