/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import java.text.SimpleDateFormat
import java.util.Date

import basicauth.{BasicAuthenticatedAction, BasicAuthenticationFilterConfiguration}
import models.ETMPNotification
import play.api.Logger
import play.api.Play._
import play.api.libs.json._
import play.api.mvc.{Action, Request, Result}
import uk.gov.hmrc.play.http.{NotFoundException, ServiceUnavailableException}
import uk.gov.hmrc.play.microservice.controller.BaseController
import _root_.util.ServiceDirector
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object NotificationController extends NotificationController {
  val director = ServiceDirector
}

trait NotificationController extends BaseController {

  val director : ServiceDirector

  val authAction = {
    val basicAuthFilterConfig = BasicAuthenticationFilterConfiguration.parse(current.mode, current.configuration)
    new BasicAuthenticatedAction(basicAuthFilterConfig)
  }

  def processNotification(ackRef : String) : Action[JsValue] = authAction.async[JsValue](parse.json) {
    implicit request =>
      withJsonBody[ETMPNotification] {
        notif =>
          director.goToService(ackRef, notif.regime, notif) map {
            case OK =>
              Ok(Json.obj(
                "result" -> "ok",
                "timestamp" -> notif.timestamp
              ))
            case otherStatus => new Status(otherStatus)
          } recover {
            case ex: NotFoundException =>
              Logger.info("[NotificationController] - [processNotification] : Acknowledgement reference not found")
              NotFound(buildFailureResponse(notif.timestamp, Some("Acknowledgement reference not found")))
            case ex : ServiceUnavailableException =>
              Logger.error(s"SERVICE UNAVAILABLE : ${ex}")
              ServiceUnavailable(buildFailureResponse(notif.timestamp))
            case ex  => {
              Logger.error(s"INTERNAL SERVER ERROR : ${ex}")
              InternalServerError(buildFailureResponse(notif.timestamp))
            }
          }
      }
  }

  def buildFailureResponse(timestamp: String, message: Option[String] = None): JsObject = {
    val response = Json.obj("result" -> "failed", "timestamp" -> timestamp)
    message match {
      case Some(m) => response ++ Json.obj("reason" -> m)
      case _ => response
    }
  }

  def timestampNow() : String = {
    val timeStampFormat = "yyyy-MM-dd'T'HH:mm:ssXXX"
    val format: SimpleDateFormat = new SimpleDateFormat(timeStampFormat)
    format.format(new Date(DateTime.now(DateTimeZone.UTC).getMillis))
  }

  override def withJsonBody[T](f: (T) => Future[Result])(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T]) =
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) =>
        Future.successful(
          BadRequest(
            buildFailureResponse(timestampNow, Some(s"Invalid ${m.runtimeClass.getSimpleName} payload: $errs"))
          )
        )
      case Failure(e) =>
        Future.successful(
          BadRequest(
            buildFailureResponse(timestampNow, Some(s"could not parse body due to ${e.getMessage}"))
          )
        )
    }

}
