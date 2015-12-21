package controllers

import org.apache.commons.lang3.StringUtils
import org.pac4j.core.authorization.Authorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.profile.HttpProfile

class CustomAuthorizer extends Authorizer[CommonProfile] {

  def isAuthorized(context: WebContext, profile: CommonProfile): Boolean = {
    if (profile != null && profile.isInstanceOf[HttpProfile]) {
      val httpProfile = profile.asInstanceOf[HttpProfile]
      val username: String = httpProfile.getUsername
      StringUtils.startsWith(username, "jle")
    } else {
      false
    }
  }
}
