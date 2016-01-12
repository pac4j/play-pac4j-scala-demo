import java.nio.charset.StandardCharsets
import java.util.Base64

import fakes.FakeCache
import play.api.cache.CacheApi
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Cookie
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}
import play.api.{Configuration, Environment}

/**
  * Created by hv01016 on 11-1-2016.
  */
class ApplicationSpec extends PlaySpecification {

  val configuration = Configuration.load(Environment.simple())

  val application = new GuiceApplicationBuilder()
    .loadConfig(configuration)
    .overrides(bind[CacheApi].to[FakeCache])
    .in(Environment.simple()).build()

  "The homepage" should {
    "not be secured" in new WithApplication(application) {
      val resp = route(FakeRequest(GET, "/")).get
      status(resp) must equalTo(OK)
      contentType(resp) must beSome.which(_ == "text/html")
      contentAsString(resp) must contain("<h1>index</h1>")
    }
  }

  "The facebook page" should {
    "be secured with the facebook client" in new WithApplication(application) {
      checkRedirect("/facebook/index.html", "facebook.com")
    }
  }

  "The facebook admin page" should {
    "be secured with the facebook client" in new WithApplication(application) {
      checkRedirect("/facebookadmin/index.html", "facebook.com")
    }
  }

  "The facebook custom page" should {
    "be secured with the facebook client" in new WithApplication(application) {
      checkRedirect("/facebookcustom/index.html", "facebook.com")
    }
  }

  "The twitter page" should {
    "be secured with the twitter client" in new WithApplication(application) {
      checkRedirect("/twitter/index.html", "twitter.com")
    }
  }

  "The form protected page" should {
    "be secured and redirects to the loginForm" in new WithApplication(application) {
      checkRedirect("/form/index.html", "loginForm")
    }
  }

//  "The form json protected page" should {
//    "be secured and sends a 401" in new WithApplication(application) {
//
//    }
//  }

  "The basic auth protected page" should {
    "be secured and redirect to callback url for basic auth" in new WithApplication(application) {
      checkRedirect("/basicauth/index.html", "callback")
    }
  }

  "The protected page" should {
    "be secured" in new WithApplication(application) {
      checkUnAuthorized("/protected/index.html")
    }
    "return ok when authorized" in new WithApplication(application) {
      val resp = route(FakeRequest(GET, "/protected/index.html").withCookies(authenticate("john"))).get
      status(resp) mustEqual OK
    }
  }

  "The protected custom page" should {
    "be secured" in new WithApplication(application) {
      checkUnAuthorized("/protected/custom.html")
    }
    "return forbidden when not custom authorized" in new WithApplication(application) {
      val resp = route(FakeRequest(GET, "/protected/custom.html").withCookies(authenticate("John"))).get
      status(resp) mustEqual FORBIDDEN
    }
    "return ok when custom authorized" in new WithApplication(application) {
      val resp = route(FakeRequest(GET, "/protected/custom.html").withCookies(authenticate("jlejohn"))).get
      status(resp) mustEqual OK
    }
  }


  def checkUnAuthorized(url: String) = {
    val resp = route(FakeRequest(GET, url)).get
    status(resp) must equalTo(UNAUTHORIZED)
  }

  def checkRedirect(url: String, target: String) = {
    val resp = route(FakeRequest(GET, url)).get
    status(resp) must equalTo(SEE_OTHER)
    redirectLocation(resp) must beSome.which(_ must contain(target))
  }

  def authenticate(user: String): Cookie = {
    val resp = route(FakeRequest(GET, "/callback?client_name=IndirectBasicAuthClient").withHeaders(getAuthHeader(user))).get
    cookies(resp).get("PLAY_SESSION").get
  }

  def getAuthHeader(user: String) = {
    val encoding = new String(Base64.getEncoder.encode(s"$user:$user".getBytes()), StandardCharsets.UTF_8)
    AUTHORIZATION -> s"Basic $encoding"
  }
}
