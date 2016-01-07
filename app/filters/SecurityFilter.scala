package filters

import java.util.Collections
import javax.inject.{Inject, Singleton}

import org.pac4j.play.PlayWebContext
import org.pac4j.play.java.RequiresAuthenticationAction
import play.api.Configuration
import play.core.j.JavaHelpers
import security.Security
import org.pac4j.core.profile.CommonProfile
import play.api.mvc._
import scala.collection.JavaConversions._

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by hv01016 on 6-1-2016.
  */
@Singleton
class SecurityFilter @Inject() (configuration: Configuration) extends Filter with Security[CommonProfile] {

  val rules = configuration.getConfigList("security.rules").getOrElse(Collections.emptyList())

  def apply(nextFilter: (RequestHeader) => Future[Result])
           (request: RequestHeader): Future[Result] = {
    findRole(request) match {
      case Some(role) if role == "_anonymous_" => nextFilter(request)
      case Some(role) =>
        val authorizerName = if (role == "_authenticated_") null else role
        val webContext = new PlayWebContext(request, config.getSessionStore)
        val requiresAuthenticationAction = new RequiresAuthenticationAction(config)
        val javaContext = webContext.getJavaContext
        requiresAuthenticationAction.internalCall(javaContext, null, authorizerName).wrapped().flatMap[play.api.mvc.Result](r =>
          if (r == null) {
            nextFilter(request)
          } else {
            Future {
              JavaHelpers.createResult(javaContext, r)
            }
          }
        )
      case None => nextFilter(request)
    }
  }

  def findRole(request: RequestHeader): Option[String] =
    rules.find{ rule =>
      val key = rule.keys.toList.head
      val regex = key.replace("/", "\\/").replace("\"", "")
      request.uri.toLowerCase.matches(regex)
    }.map(r => r.getString(r.keys.toList.head).get)
}
