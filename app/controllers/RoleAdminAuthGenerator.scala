package controllers

import java.util.Optional

import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.UserProfile

class RoleAdminAuthGenerator extends AuthorizationGenerator {

  override def generate(context: WebContext, profile: UserProfile): Optional[UserProfile] = {
    profile.addRole("ROLE_ADMIN")
    Optional.of(profile)
  }
}
