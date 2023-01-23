package controllers

import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.CallContext
import org.pac4j.core.profile.UserProfile

import java.util.Optional

class RoleAdminAuthGenerator extends AuthorizationGenerator {

  override def generate(ctx: CallContext, profile: UserProfile): Optional[UserProfile] = {
    profile.addRole("ROLE_ADMIN")
    Optional.of(profile)
  }
}
