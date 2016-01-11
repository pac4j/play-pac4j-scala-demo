import _root_.fakes.FakeCache
import play.api.cache.CacheApi
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
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
    }
  }

  "The protected page" should {
    "be secured" in new WithApplication(application) {
      val resp = route(FakeRequest(GET, "/protected/index.html")).get
      status(resp) must equalTo(UNAUTHORIZED)
    }
  }

}
