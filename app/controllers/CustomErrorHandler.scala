package controllers

import play.api.http.HttpErrorHandler
import play.api.mvc.RequestHeader

import scala.concurrent.Future

import play.api.mvc.Results._

class CustomErrorHandler extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful(
      InternalServerError(views.html.error500())
    )
  }
}
