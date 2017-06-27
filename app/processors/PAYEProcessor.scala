/*
 * Copyright 2017 HM Revenue & Customs
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

package processors

import javax.inject.{Inject, Singleton}

import audit.builders.AuditBuilding
import audit.events.ProcessedNotificationEvent
import config.MicroserviceAuditConnector
import models.{ETMPNotification, PAYERegistrationPost}
import constants.Outcome
import services.RegistrationService
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class PAYEProcessor @Inject()(
                               auditConnector: MicroserviceAuditConnector,
                               registrationService: RegistrationService
                               ) extends RegimeProcessor with AuditBuilding {

  private[processors] def notificationToPAYEPost(notification: ETMPNotification): PAYERegistrationPost = {
    PAYERegistrationPost(notification.taxId, notification.timestamp, notification.status)
  }

  override def processRegime(ackRef: String, data: ETMPNotification)(implicit hc: HeaderCarrier): Future[Int] = {
    val auditRef = if(Outcome.successfulOutcome(data)) "successfulTaxServiceRegistration" else "rejectedTaxServiceRegistration"
    auditConnector.sendEvent(
      new ProcessedNotificationEvent(
        auditRef, buildAuditEventDetail(ackRef, data), Some("payeRegistrationUpdateRequest"))
    ) recover {
      case e => throw new AuditError(e.getMessage)
    } flatMap {
      _ => registrationService.sendToPAYERegistration(ackRef, notificationToPAYEPost(data))
    } recoverWith {
      case err => handleProcessRegimeError(
        err,
        registrationService.sendToPAYERegistration(ackRef, notificationToPAYEPost(data)),
        "[PAYEProcessor] - [processRegime]"
      )
    }
  }
}
