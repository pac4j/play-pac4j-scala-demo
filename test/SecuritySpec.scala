import java.nio.charset.StandardCharsets
import java.util.Base64

import fakes.FakeCache
import play.api.cache.SyncCacheApi
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Cookie
import play.api.test.{FakeRequest, PlaySpecification}
import play.api.{Application, Configuration, Environment, Play}

/**
  * Generic security tests. Can be implemented by making a subclass and implementing the base url.
  *
  * @author Hugo Valk
  */
trait SecuritySpec extends PlaySpecification {

  def baseUrl: String

  val configuration = Configuration.load(Environment.simple())

  override lazy val baseApplicationBuilder = {
    new GuiceApplicationBuilder()
    .loadConfig(configuration)
    .overrides(bind[SyncCacheApi].to[FakeCache])
    .in(Environment.simple())
  }

  running()(implicit app => {

    "The facebook page" should {
      "be secured with the facebook client" in {
        checkRedirect(s"$baseUrl/facebook/index.html", "facebook.com")
      }
    }

    "The facebook admin page" should {
      "be secured with the facebook client" in {
        checkRedirect(s"$baseUrl/facebookadmin/index.html", "facebook.com")  
      }
    }

    "The facebook custom page" should {
      "be secured with the facebook client" in {
        checkRedirect(s"$baseUrl/facebookcustom/index.html", "facebook.com")
      }
    }

    "The twitter page" should {
      "be secured with the twitter client" in {
        checkRedirect(s"$baseUrl/twitter/index.html", "twitter.com")
      }
    }

    "The form protected page" should {
      "be secured and redirects to the loginForm" in {
        checkRedirect(s"$baseUrl/form/index.html", "loginForm")
      }
    }

//  "The form json protected page" should {
//    "be secured and sends a 401" in new WithApplication(application) {
//
//    }
//  }

    "The basic auth protected page" should {
      "be secured and redirect to callback url for basic auth" in {
        checkRedirect(s"$baseUrl/basicauth/index.html", "callback")
      }
    }

    "The protected page" should {
      "be secured" in {
        checkUnAuthorized(s"$baseUrl/protected/index.html")
      }
      "return ok when authorized" in {
        val resp = route(app, FakeRequest(GET, s"$baseUrl/protected/index.html").withCookies(authenticate("john"))).get
        status(resp) mustEqual OK
      }
    }

    "The protected custom page" should {
      "be secured" in {
        checkUnAuthorized(s"$baseUrl/protected/custom.html")
      }
      "return forbidden when not custom authorized" in {
        val resp = route(app, FakeRequest(GET, s"$baseUrl/protected/custom.html").withCookies(authenticate("John"))).get
        status(resp) mustEqual FORBIDDEN
      }
      "return ok when custom authorized" in {
        val resp = route(app, FakeRequest(GET, s"$baseUrl/protected/custom.html").withCookies(authenticate("jlejohn"))).get
        status(resp) mustEqual OK
      }
    }
  })


  def checkUnAuthorized(url: String)(implicit app: Application) = {
    val resp = route(app, FakeRequest(GET, url)).get
    status(resp) must equalTo(UNAUTHORIZED)
  }

  def checkRedirect(url: String, target: String)(implicit app: Application) = {
    val resp = route(app, FakeRequest(GET, url)).get
    status(resp) must equalTo(SEE_OTHER)
    redirectLocation(resp) must beSome.which(_ must contain(target))
  }

  def authenticate(user: String)(implicit app: Application): Cookie = {
    val resp = route(app, FakeRequest(GET, "/callback?client_name=IndirectBasicAuthClient").withHeaders(getAuthHeader(user))).get
    cookies(resp).get("PLAY_SESSION").get
  }

  def getAuthHeader(user: String) = {
    val encoding = new String(Base64.getEncoder.encode(s"$user:$user".getBytes()), StandardCharsets.UTF_8)
    AUTHORIZATION -> s"Basic $encoding"
  }
}
