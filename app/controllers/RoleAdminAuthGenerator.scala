package controllers

import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.oidc.profile.OidcProfile

class RoleAdminAuthGenerator extends AuthorizationGenerator[OidcProfile] {

  override def generate(profile: OidcProfile): Unit = {
    profile.addRole("ROLE_ADMIN")
  }
}
