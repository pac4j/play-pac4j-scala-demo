package modules

import com.google.inject.AbstractModule
import controllers.DemoHttpActionHandler
import org.pac4j.cas.client.CasClient
import org.pac4j.core.client.Clients
import org.pac4j.core.profile.UserProfile
import org.pac4j.http.client.direct.ParameterClient
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.HttpCredentials
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.http.profile.creator.AuthenticatorProfileCreator
import org.pac4j.http.profile.creator.test.SimpleTestUsernameProfileCreator
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.oauth.client.{TwitterClient, FacebookClient}
import org.pac4j.oidc.client.OidcClient
import org.pac4j.play.store.{DataStore, CacheStore}
import org.pac4j.play.{ApplicationLogoutController, CallbackController}
import org.pac4j.play.handler.{HttpActionHandler}
import org.pac4j.saml.client.SAML2ClientConfiguration
import play.api.{ Configuration, Environment }
import java.io.File
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
    val casUrl = configuration.getString("casUrl").get

    // OAuth
    val facebookClient = new FacebookClient(fbId, fbSecret)
    val twitterClient = new TwitterClient("HVSQGAw2XmiwcKOTvZFbQ", "FSiO9G9VRR4KCuksky0kgGuo8gAVndYymr4Nl7qc8AA")
    // HTTP
    val formClient = new FormClient(baseUrl + "/theForm",
      new SimpleTestUsernamePasswordAuthenticator(), new SimpleTestUsernameProfileCreator())
    val basicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator(), new SimpleTestUsernameProfileCreator())

    // CAS
    val casClient = new CasClient()
    // casClient.setLogoutHandler(new PlayLogoutHandler())
    // casClient.setCasProtocol(CasProtocol.SAML)
    // casClient.setGateway(true)
    /*val casProxyReceptor = new CasProxyReceptor()
    casProxyReceptor.setCallbackUrl("http://localhost:9000/casProxyCallback")
    casClient.setCasProxyReceptor(casProxyReceptor)*/
    casClient.setCasLoginUrl(casUrl)

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
    val parameterClient = new ParameterClient("token", new JwtAuthenticator("12345678901234567890123456789012"), new AuthenticatorProfileCreator[HttpCredentials, UserProfile])
    parameterClient.setSupportGetRequest(true)
    parameterClient.setSupportPostRequest(false)

    val clients = new Clients(baseUrl + "/callback", facebookClient, twitterClient, formClient,
      basicAuthClient, casClient, saml2Client, oidcClient, parameterClient) // , casProxyReceptor);

    val config = new Config()
    config.setClients(clients)
    bind(classOf[Config]).toInstance(config)

    val cacheStore = new CacheStore()
    // for test purposes: profile timeout = 60 seconds
    //cacheStore.setProfileTimeout(60)
    bind(classOf[DataStore]).toInstance(cacheStore)

    // extra HTTP action handler
    bind(classOf[HttpActionHandler]).to(classOf[DemoHttpActionHandler])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/")
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new ApplicationLogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[ApplicationLogoutController]).toInstance(logoutController)
  }
}
