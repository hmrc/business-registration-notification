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

package processors

import audit.builders.AuditBuilding
import audit.events.ProcessedNotificationEvent
import constants.Outcome
import models.{ETMPNotification, PAYERegistrationPost}
import services.CompanyRegistrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PAYEProcessor @Inject()(auditConnector: AuditConnector,
                              registrationService: CompanyRegistrationService
                             )(implicit ec: ExecutionContext) extends RegimeProcessor with AuditBuilding {

  private[processors] def notificationToPAYEPost(notification: ETMPNotification): PAYERegistrationPost = {
    PAYERegistrationPost(notification.taxId, notification.timestamp, notification.status)
  }

  override def processRegime(ackRef: String, data: ETMPNotification)(implicit hc: HeaderCarrier): Future[Int] = {

    val auditRef = if (Outcome.successfulOutcome(data)) "successfulTaxServiceRegistration" else "rejectedTaxServiceRegistration"

    auditConnector.sendExtendedEvent(
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
