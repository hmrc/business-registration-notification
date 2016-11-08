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

import basicauth.{BasicAuthenticatedAction, BasicAuthenticationFilterConfiguration}
import models.ETMPNotification
import play.api.Logger
import play.api.Play._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Action
import uk.gov.hmrc.play.http.{NotFoundException, ServiceUnavailableException}
import uk.gov.hmrc.play.microservice.controller.BaseController
import util.ServiceDirector

import scala.concurrent.ExecutionContext.Implicits.global

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
              NotFound(Json.obj(
                "result"->"failed",
                "timestamp" -> notif.timestamp,
                "reason" -> "Acknowledgement reference not found"
              ))
            case ex : ServiceUnavailableException =>
              Logger.error(s"SERVICE UNAVAILABLE : ${ex}")
              ServiceUnavailable(
                Json.obj(
                  "result"->"failed",
                  "timestamp" -> notif.timestamp
                )
              )
            case ex  => {
              Logger.error(s"INTERNAL SERVER ERROR : ${ex}")
              InternalServerError(
                Json.obj(
                  "result"->"failed",
                  "timestamp" -> notif.timestamp
                )
              )
            }
          }
      }
  }
}
