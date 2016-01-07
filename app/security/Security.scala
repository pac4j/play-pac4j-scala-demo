/*
  Copyright 2012 - 2015 pac4j organization

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package security

import javax.inject.Inject

import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.profile._
import org.pac4j.play.java.RequiresAuthenticationAction
import play.api.mvc._
import play.core.j.JavaHelpers

import scala.collection.JavaConverters
import _root_.scala.concurrent.Future
import org.pac4j.play._
import org.slf4j._

import play.api.libs.concurrent.Execution.Implicits._

/**
  * <p>This trait adds security features to your Scala controllers.</p>
  * <p>For manual computation of login urls (redirections to identity providers), the session must be first initialized using the {@link #getOrCreateSessionId} method.</p>
  * <p>To protect a resource, the {@link #RequiresAuthentication} methods must be used.</p>
  * <p>To get the current user profile, the {@link #getUserProfile} method must be called.</p>
  *
  * @author Jerome Leleu
  * @author Michael Remond
  * @author Hugo Valk
  * @since 1.5.0
  */
trait Security[P<:CommonProfile] {

  protected val logger = LoggerFactory.getLogger(getClass)

  @Inject
  protected var config: Config = null

  /**
    * Return the current user profile.
    *
    * @param request
    * @return the user profile
    */
  protected def getUserProfile(implicit request: RequestHeader): Option[P] = {
    val webContext = new PlayWebContext(request, config.getSessionStore)
    val profileManager = new ProfileManager[P](webContext)
    Option(profileManager.get(true))
  }
}
