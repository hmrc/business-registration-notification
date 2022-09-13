/*
 * Copyright 2022 HM Revenue & Customs
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

import _root_.util.ServiceDirector
import basicauth.{BasicAuthenticatedAction, BasicAuthentication}
import models.ETMPNotification
import play.api.{Configuration, Logging}
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents, Request, Result}
import services.MetricsService
import uk.gov.hmrc.http.{NotFoundException, ServiceUnavailableException}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class NotificationController @Inject()(val metrics: MetricsService,
                                       val config: Configuration,
                                       cc: ControllerComponents,
                                       director: ServiceDirector
                                      )(implicit ec: ExecutionContext) extends BackendController(cc) with BasicAuthentication with Logging {

  val authAction = new BasicAuthenticatedAction(getBasicAuthConfig, cc)

  def processNotification(ackRef: String): Action[JsValue] = authAction.async[JsValue](parse.json) {
    implicit request =>
      withJsonBody[ETMPNotification] {
        notif =>
          logger.info(s"[NotificationController] - ETMP sent request with ackref : $ackRef and status : ${notif.status} for regime : ${notif.regime}")
          director.goToService(ackRef, notif.regime, notif) map {
            case OK =>
              metrics.etmpNotificationCounter.inc(1)
              Ok(Json.obj(
                "result" -> "ok",
                "timestamp" -> notif.timestamp
              ))
            case otherStatus => new Status(otherStatus)
          } recover {
            case _: NotFoundException =>
              logger.info("[NotificationController] - [processNotification] : Acknowledgement reference not found")
              metrics.ackRefNotFound.inc(1)
              metrics.clientErrorCodes.inc(1)
              NotFound(buildFailureResponse(notif.timestamp, Some("Acknowledgement reference not found")))
            case ex: ServiceUnavailableException =>
              logger.error(s"SERVICE UNAVAILABLE : $ex")
              metrics.serviceNotAvailable.inc(1)
              metrics.serverErrorCodes.inc(1)
              ServiceUnavailable(buildFailureResponse(notif.timestamp))
            case ex =>
              logger.error(s"INTERNAL SERVER ERROR : $ex")
              metrics.internalServerError.inc(1)
              metrics.serverErrorCodes.inc(1)
              InternalServerError(buildFailureResponse(notif.timestamp))
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

  def timestampNow: String = {
    val timeStampFormat = "yyyy-MM-dd'T'HH:mm:ssXXX"
    val format: SimpleDateFormat = new SimpleDateFormat(timeStampFormat)
    format.format(new Date(Instant.now.toEpochMilli))
  }

  override def withJsonBody[T](f: T => Future[Result])
                              (implicit request: Request[JsValue],
                               m: Manifest[T],
                               reads: Reads[T]): Future[Result] =
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) =>
        val message = s"Invalid ${m.runtimeClass.getSimpleName} payload: $errs"
        logger.info(message)
        Future.successful(
          BadRequest(
            buildFailureResponse(timestampNow, Some(message))
          )
        )
      case Failure(e) =>
        val message = s"could not parse body due to ${e.getMessage}"
        logger.warn(message)
        Future.successful(
          BadRequest(
            buildFailureResponse(timestampNow, Some(message))
          )
        )
    }
}
