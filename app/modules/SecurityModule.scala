package modules

import com.google.inject.AbstractModule
import security.SecurityConfig
import security.impl.SecurityConfigAll

/**
 * Guice DI module to be included in application.conf
 */
class SecurityModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[SecurityConfig]).to(classOf[SecurityConfigAll]).asEagerSingleton()
  }

}
