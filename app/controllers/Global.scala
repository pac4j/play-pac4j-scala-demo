package controllers

import scala.concurrent.Future
import play.api._
import play.api.Play.current
import play.api.mvc._
import org.pac4j.play._
import org.pac4j.core.client._
import org.pac4j.core.context._
import org.pac4j.cas.client._
import org.pac4j.saml.client._
import org.pac4j.oauth.client._
import org.pac4j.http.client._
import org.pac4j.http.profile._
import org.pac4j.oidc.client._
import org.pac4j.http.credentials._
import play.api.mvc.Results._

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, t: Throwable) = {
    Future.successful(InternalServerError(
      views.html.error500.render()
    ))
  }

  override def onStart(app: Application) {
    BaseConfig.setErrorPage401(views.html.error401.render().toString())
    BaseConfig.setErrorPage403(views.html.error403.render().toString())
    
    val fbId = Play.application.configuration.getString("fbId").get
    val fbSecret = Play.application.configuration.getString("fbSecret").get
    val baseUrl = Play.application.configuration.getString("baseUrl").get
    val casUrl = Play.application.configuration.getString("casUrl").get

    // OAuth
    val facebookClient = new FacebookClient(fbId, fbSecret)
    val twitterClient = new TwitterClient("HVSQGAw2XmiwcKOTvZFbQ", "FSiO9G9VRR4KCuksky0kgGuo8gAVndYymr4Nl7qc8AA")

    // HTTP
    val formClient = new FormClient(baseUrl + "/theForm", new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator())
    val basicAuthClient = new BasicAuthClient(new SimpleTestUsernamePasswordAuthenticator(), new UsernameProfileCreator())
        
    // CAS
    val casClient = new CasClient()
    //casClient.setGateway(true)
    //casClient.setLogoutHandler(new PlayLogoutHandler())
    casClient.setCasLoginUrl(casUrl)
    
    // SAML
    val saml2Client = new Saml2Client()
    saml2Client.setKeystorePath("resource:samlKeystore.jks")
    saml2Client.setKeystorePassword("pac4j-demo-passwd")
    saml2Client.setPrivateKeyPassword("pac4j-demo-passwd")
    saml2Client.setIdpMetadataPath("resource:openidp-feide.xml")

	// OpenID Connect
	val oidcClient = new OidcClient()
	oidcClient.setClientID("343992089165-i1es0qvej18asl33mvlbeq750i3ko32k.apps.googleusercontent.com");
    oidcClient.setSecret("unXK_RSCbCXLTic2JACTiAo9");
    oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration");
    oidcClient.addCustomParam("prompt", "consent");
	    
    val clients = new Clients(baseUrl + "/callback", facebookClient, twitterClient, formClient, basicAuthClient, casClient, saml2Client, oidcClient)
    Config.setClients(clients)
    // for test purposes : profile timeout = 60 seconds
    // Config.setProfileTimeout(60)
  }  
}
