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

  val rules = configuration.getConfigList("security.rules")
    .getOrElse(Collections.emptyList())

  def apply(nextFilter: (RequestHeader) => Future[Result])
           (request: RequestHeader): Future[Result] = {
    findRule(request) match {
      case Some(rule) =>
        val webContext = new PlayWebContext(request, config.getSessionStore)
        val requiresAuthenticationAction = new RequiresAuthenticationAction(config)
        val javaContext = webContext.getJavaContext
        requiresAuthenticationAction.internalCall(javaContext, rule.clientNames, rule.authorizerNames).wrapped().flatMap[play.api.mvc.Result](r =>
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

  def findRule(request: RequestHeader): Option[Rule] =
    rules.find{ rule =>
      val key = rule.subKeys.head
      val regex = key.replace("\"", "")
      request.uri.matches(regex)
    }.flatMap(configurationToRule)

  def configurationToRule(c: Configuration): Option[Rule] = {
    c.getConfig("\"" + c.subKeys.head + "\"").flatMap{rule =>
      val res = new Rule(rule.getString("clients").orNull, rule.getString("authorizers").orNull)
      if (res.authorizerNames == "_anonymous_")
        None
      else if (res.authorizerNames == "_authenticated_")
        Some(res.copy(authorizerNames = null))
      else Some(res)
    }
  }

  case class Rule(clientNames: String, authorizerNames: String)
}

