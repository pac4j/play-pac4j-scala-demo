package controllers

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future
import play.api.mvc.Results._

class CustomErrorHandler extends HttpErrorHandler {

  private val log = Logger(this.getClass)

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful {
      log.error("Error occurrred", exception)
      InternalServerError(views.html.error500())
    }
  }
}
