package modules

import com.google.inject.AbstractModule
import controllers.{CustomAuthorizer, DemoHttpActionAdapter}
import org.pac4j.cas.client.CasClient
import org.pac4j.cas.client.CasClient.CasProtocol
import org.pac4j.core.client.Clients
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.oauth.client.{FacebookClient, TwitterClient}
import org.pac4j.oidc.client.OidcClient
import org.pac4j.play.cas.logout.PlayCacheLogoutHandler
import org.pac4j.play.{ApplicationLogoutController, CallbackController}
import org.pac4j.saml.client.SAML2ClientConfiguration
import play.api.{Configuration, Environment}
import java.io.File

import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.config.Config
import org.pac4j.saml.client.SAML2Client

/**
 * Guice DI module to be included in application.conf
 */
class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure(): Unit = {

    val fbId = configuration.getString("fbId").get
    val fbSecret = configuration.getString("fbSecret").get
    val baseUrl = configuration.getString("baseUrl").get

    // OAuth
    val facebookClient = new FacebookClient(fbId, fbSecret)
    val twitterClient = new TwitterClient("HVSQGAw2XmiwcKOTvZFbQ", "FSiO9G9VRR4KCuksky0kgGuo8gAVndYymr4Nl7qc8AA")
    // HTTP
    val formClient = new FormClient(baseUrl + "/loginForm", new SimpleTestUsernamePasswordAuthenticator())
    val indirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())

    // CAS
    val casClient = new CasClient("https://casserverpac4j.herokuapp.com/login")
    casClient.setLogoutHandler(new PlayCacheLogoutHandler())
    casClient.setCasProtocol(CasProtocol.CAS20)
    // casClient.setGateway(true)
    /*val casProxyReceptor = new CasProxyReceptor()
    casProxyReceptor.setCallbackUrl("http://localhost:9000/casProxyCallback")
    casClient.setCasProxyReceptor(casProxyReceptor)*/

    // SAML
    val cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:openidp-feide.xml")
    cfg.setMaximumAuthenticationLifetime(3600)
    cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org")
    cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath())
    val saml2Client = new SAML2Client(cfg)

    // OpenID Connect
    val oidcClient = new OidcClient()
    oidcClient.setClientID("343992089165-i1es0qvej18asl33mvlbeq750i3ko32k.apps.googleusercontent.com")
    oidcClient.setSecret("unXK_RSCbCXLTic2JACTiAo9")
    oidcClient.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
    oidcClient.addCustomParam("prompt", "consent")

    // REST authent with JWT for a token passed in the url as the token parameter
    val parameterClient = new ParameterClient("token", new JwtAuthenticator("12345678901234567890123456789012"))
    parameterClient.setSupportGetRequest(true)
    parameterClient.setSupportPostRequest(false)

    // basic auth
    val directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator)

    val clients = new Clients(baseUrl + "/callback", facebookClient, twitterClient, formClient,
      indirectBasicAuthClient, casClient, saml2Client, oidcClient, parameterClient, directBasicAuthClient) // , casProxyReceptor);

    val config = new Config(clients)
    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer[Nothing]("ROLE_ADMIN"))
    config.addAuthorizer("custom", new CustomAuthorizer)
    config.setHttpActionAdapter(new DemoHttpActionAdapter())
    bind(classOf[Config]).toInstance(config)

    // for test purposes: profile timeout = 60 seconds
    // config.getSessionStore.asInstanceOf[PlayCacheStore].setProfileTimeout(60)

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/?defaulturlafterlogout")
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new ApplicationLogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[ApplicationLogoutController]).toInstance(logoutController)
  }
}
