import fakes.FakeCache
import play.api.cache.CacheApi
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Environment, Configuration}
import play.api.test.{FakeRequest, WithApplication, PlaySpecification}

/**
  * Created by hv01016 on 14-1-2016.
  */
class HomePageSpec extends PlaySpecification {
  val configuration = Configuration.load(Environment.simple())

  def application = {
    new GuiceApplicationBuilder()
    .loadConfig(configuration)
    .overrides(bind[CacheApi].to[FakeCache])
    .in(Environment.simple()).build()
  }

  "The homepage" should {
    "not be secured" in new WithApplication(application) {
      val resp = route(app, FakeRequest(GET, "/")).get
      status(resp) must equalTo(OK)
      contentType(resp) must beSome.which(_ == "text/html")
      contentAsString(resp) must contain("<h1>index</h1>")
    }
  }
}