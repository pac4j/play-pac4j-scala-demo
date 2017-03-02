package controllers

import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.oidc.profile.OidcProfile

class RoleAdminAuthGenerator extends AuthorizationGenerator[OidcProfile] {

  override def generate(context: WebContext, profile: OidcProfile): OidcProfile = {
    profile.addRole("ROLE_ADMIN")
    profile
  }
}
