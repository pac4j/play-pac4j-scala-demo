package modules

import com.google.inject.{AbstractModule, Provides}
import controllers.{CustomAuthorizer, DemoHttpActionAdapter, RoleAdminAuthGenerator}
import org.pac4j.cas.client.{CasClient, CasProxyReceptor}
import org.pac4j.cas.config.{CasConfiguration, CasProtocol}
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer
import org.pac4j.core.client.Clients
import org.pac4j.core.client.direct.AnonymousClient
import org.pac4j.core.config.Config
import org.pac4j.core.context.FrameworkParameters
import org.pac4j.core.context.session.{SessionStore, SessionStoreFactory}
import org.pac4j.core.matching.matcher.PathMatcher
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.{FormClient, IndirectBasicAuthClient}
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.oauth.client.{FacebookClient, TwitterClient}
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.play.scala.{DefaultSecurityComponents, Pac4jScalaTemplateHelper, SecurityComponents}
import org.pac4j.play.store.{PlayCookieSessionStore, ShiroAesDataEncrypter}
import org.pac4j.play.{CallbackController, LogoutController}
import org.pac4j.saml.client.SAML2Client
import org.pac4j.saml.config.SAML2Configuration
import play.api.{Configuration, Environment}

import java.io.File
import java.nio.charset.StandardCharsets

/**
  * Guice DI module to be included in application.conf
  */
class SecurityModule(environment: Environment, configuration: Configuration) extends AbstractModule {

  val baseUrl = configuration.get[String]("baseUrl")

  override def configure(): Unit = {

    val sKey = configuration.get[String]("play.http.secret.key").substring(0, 16)
    val dataEncrypter = new ShiroAesDataEncrypter(sKey.getBytes(StandardCharsets.UTF_8))
    val playSessionStore = new PlayCookieSessionStore(dataEncrypter)
    bind(classOf[SessionStore]).toInstance(playSessionStore)

    bind(classOf[SecurityComponents]).to(classOf[DefaultSecurityComponents])

    bind(classOf[Pac4jScalaTemplateHelper[CommonProfile]])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/?defaulturlafterlogout")
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)
  }

  @Provides
  def provideFacebookClient: FacebookClient = {
    val fbId = configuration.getOptional[String]("fbId").get
    val fbSecret = configuration.getOptional[String]("fbSecret").get
    val fbClient = new FacebookClient(fbId, fbSecret)
    fbClient.setMultiProfile(true)
    fbClient
  }

  @Provides
  def provideTwitterClient: TwitterClient = new TwitterClient("HVSQGAw2XmiwcKOTvZFbQ", "FSiO9G9VRR4KCuksky0kgGuo8gAVndYymr4Nl7qc8AA")

  @Provides
  def provideFormClient: FormClient = new FormClient(baseUrl + "/loginForm", new SimpleTestUsernamePasswordAuthenticator())

  @Provides
  def provideIndirectBasicAuthClient: IndirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())

  /*@Provides
  def provideCasProxyReceptor: CasProxyReceptor = new CasProxyReceptor()*/

  @Provides
  def provideCasClient(casProxyReceptor: CasProxyReceptor): CasClient = {
    val casConfiguration = new CasConfiguration("https://casserverpac4j.herokuapp.com/login")
    //val casConfiguration = new CasConfiguration("http://localhost:8888/cas/login")
    casConfiguration.setProtocol(CasProtocol.CAS20)
    //casConfiguration.setProxyReceptor(casProxyReceptor)
    new CasClient(casConfiguration)
  }

  @Provides
  def provideSaml2Client: SAML2Client = {
    val cfg = new SAML2Configuration("resource:samlKeystore.jks", "pac4j-demo-passwd", "pac4j-demo-passwd", "resource:openidp-feide.xml")
    cfg.setMaximumAuthenticationLifetime(3600)
    cfg.setServiceProviderEntityId("urn:mace:saml:pac4j.org")
    cfg.setServiceProviderMetadataPath(new File("target", "sp-metadata.xml").getAbsolutePath)
    new SAML2Client(cfg)
  }

  @Provides
  def provideOidcClient: OidcClient = {
    val oidcConfiguration = new OidcConfiguration()
    oidcConfiguration.setClientId("343992089165-i1es0qvej18asl33mvlbeq750i3ko32k.apps.googleusercontent.com")
    oidcConfiguration.setSecret("unXK_RSCbCXLTic2JACTiAo9")
    oidcConfiguration.setDiscoveryURI("https://accounts.google.com/.well-known/openid-configuration")
    oidcConfiguration.addCustomParam("prompt", "consent")
    val oidcClient = new OidcClient(oidcConfiguration)
    oidcClient.addAuthorizationGenerator(new RoleAdminAuthGenerator)
    oidcClient
  }

  @Provides
  def provideParameterClient: ParameterClient = {
    val jwtAuthenticator = new JwtAuthenticator()
    jwtAuthenticator.addSignatureConfiguration(new SecretSignatureConfiguration("12345678901234567890123456789012"))
    val parameterClient = new ParameterClient("token", jwtAuthenticator)
    parameterClient.setSupportGetRequest(true)
    parameterClient.setSupportPostRequest(false)
    parameterClient
  }

  @Provides
  def provideDirectBasicAuthClient: DirectBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator)

  @Provides
  def provideConfig(facebookClient: FacebookClient, twitterClient: TwitterClient, formClient: FormClient, indirectBasicAuthClient: IndirectBasicAuthClient,
                    casClient: CasClient, saml2Client: SAML2Client, oidcClient: OidcClient, parameterClient: ParameterClient, directBasicAuthClient: DirectBasicAuthClient,
                    sessionStore: SessionStore): Config = {
    val clients = new Clients(baseUrl + "/callback", facebookClient, twitterClient, formClient,
      indirectBasicAuthClient, casClient, saml2Client, oidcClient, parameterClient, directBasicAuthClient,
      new AnonymousClient())

    val config = new Config(clients)
    config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"))
    config.addAuthorizer("custom", new CustomAuthorizer)
    config.addMatcher("excludedPath", new PathMatcher().excludeRegex("^/facebook/notprotected\\.html$"))
    config.setSessionStoreFactory(new SessionStoreFactory {
      override def newSessionStore(parameters: FrameworkParameters): SessionStore = sessionStore
    });
    config.setHttpActionAdapter(new DemoHttpActionAdapter())
    config
  }
}
