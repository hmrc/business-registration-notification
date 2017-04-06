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

import audit.events.{ProcessedNotificationEvent, ProcessedNotificationEventDetail}
import config.MicroserviceAuditConnector
import models.{CompanyRegistrationPost, ETMPNotification}
import play.api.Logger
import services.RegistrationService
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Failure
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CTProcessor @Inject()(auditConnector: MicroserviceAuditConnector, registrationService: RegistrationService) extends RegimeProcessor {

  private[processors] def notificationToCRPost(notification: ETMPNotification): CompanyRegistrationPost = {
    CompanyRegistrationPost(notification.taxId, notification.timestamp, notification.status)
  }

  private[processors] def buildAuditEventDetail(ackRef: String, data: ETMPNotification): ProcessedNotificationEvent = {
    new ProcessedNotificationEvent(
      ProcessedNotificationEventDetail(
        ackRef,
        data.timestamp,
        data.regime,
        data.taxId,
        data.status
      )
    )
  }

  override def processRegime(ackRef: String, data: ETMPNotification)(implicit hc: HeaderCarrier): Future[Int] = {
    auditConnector.sendEvent(buildAuditEventDetail(ackRef, data)) flatMap {
      case Failure(msg, _) =>
        Logger.error(s"[CTProcessor] - [processRegime]: Audit event failed because $msg")
        registrationService.sendToCompanyRegistration(ackRef, notificationToCRPost(data))
      case _ => registrationService.sendToCompanyRegistration(ackRef, notificationToCRPost(data))
    }
  }
}
