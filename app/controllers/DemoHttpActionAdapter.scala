package controllers

import org.pac4j.core.context.HttpConstants
import org.pac4j.core.exception.http.HttpAction
import org.pac4j.play.PlayWebContext
import org.pac4j.play.http.PlayHttpActionAdapter
import play.mvc.Results
import play.mvc.Result

class DemoHttpActionAdapter extends PlayHttpActionAdapter {

  override def adapt(action: HttpAction, context: PlayWebContext): Result = {
    if (action != null && action.getCode == HttpConstants.UNAUTHORIZED) {
      Results.unauthorized(views.html.error401.render().toString()).as(HttpConstants.HTML_CONTENT_TYPE)
    } else if (action != null && action.getCode == HttpConstants.FORBIDDEN) {
      Results.forbidden(views.html.error403.render().toString()).as(HttpConstants.HTML_CONTENT_TYPE)
    } else {
      super.adapt(action, context)
    }
  }
}
