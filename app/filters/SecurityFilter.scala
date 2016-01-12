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
  * Filter on all requests to apply security by the Pac4J framework.
  *
  * Rules for the security filter can be supplied in application.conf. An example is shown below. It
  * consists of a list of filter rules, where the key is a regular expression that will be used to
  * match the url. Make sure that the / is escaped by \\ to make a valid regular expression.
  *
  * For each regex key, there are two subkeys: `authorizers` and `clients`. Here you can define the
  * correct values, like you would supply to the `RequireAuthentication` method in controllers. There
  * two exceptions: `authorizers` can have two special values: `_authenticated_` and `_anonymous_`.
  *
  * `_anonymous_` will disable authentication and authorization for urls matching the regex.
  * `_authenticated_` will require authentication, but will set clients and authorizers both to `null`.
  *
  * Rules are applied top to bottom. The first matching rule will define which clients and authorizers
  * are used. When not provided, the value will be `null`.
  *
  * @example {{{
  *           security.rules = [
  *             # Admin pages need a special authorizer and do not support login via Twitter.
  *             {"\\/admin\\/.*" = {
  *               authorizers = "admin"
  *               clients = "FormClient"
  *             }}
  *             # Rules for the REST services. These don't specify a client and will return 401
  *             # when not authenticated.
  *             {"\\/restservices\\/.*" = {
  *               authorizers = "_authenticated_"
  *             }}
  *             # The login page needs to be publicly accessible.
  *             {"\\/login.html" = {
  *               authorizers = "_anonymous_"
  *             }}
  *             # 'Catch all' rule to make sure the whole application stays secure.
  *             {".*" = {
  *               authorizers = "_authenticated_"
  *               clients = "FormClient,TwitterClient"
  *             }}
  *           ]
  *          }}}
  *
  * @see http://www.pac4j.org/
  * @see https://github.com/pac4j/play-pac4j
  *
  * @author Hugo Valk
  */
@Singleton
class SecurityFilter @Inject()(configuration: Configuration) extends Filter with Security[CommonProfile] {

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
    rules.find { rule =>
      val key = rule.subKeys.head
      val regex = key.replace("\"", "")
      request.uri.matches(regex)
    }.flatMap(configurationToRule)

  def configurationToRule(c: Configuration): Option[Rule] = {
    c.getConfig("\"" + c.subKeys.head + "\"").flatMap { rule =>
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

