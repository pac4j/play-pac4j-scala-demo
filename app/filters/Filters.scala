package filters

import javax.inject.Inject

import play.api.http.HttpFilters

/**
 * Configuration of all the Play filters that are used in the application.
 */
class Filters @Inject()(securityFilter: SecurityFilter) extends HttpFilters {

  def filters = Seq(securityFilter)

}
