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

import models.ETMPNotification
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent}
import services.CompanyRegistrationService
import uk.gov.hmrc.play.microservice.controller.BaseController
import util.ServiceDirector

import scala.concurrent.ExecutionContext.Implicits.global

object NotificationController extends NotificationController {
  val director = ServiceDirector
}

trait NotificationController extends BaseController {

  val director : ServiceDirector

  def processNotification(ackRef : String) : Action[JsValue] = Action.async[JsValue](parse.json) {
    implicit request =>
      withJsonBody[ETMPNotification] {
        notif =>
          director.goToService(ackRef, notif.regime, notif) map {
            status => Status(status)
          }
      }
  }
}
