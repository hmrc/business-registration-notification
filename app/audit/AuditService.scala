/*
 * Copyright 2023 HM Revenue & Customs
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

package audit

import java.time.Instant
import java.util.UUID

import javax.inject.Inject
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(auditConnector: AuditConnector) {

  private[audit] def now() = Instant.now()
  private[audit] def eventId() = UUID.randomUUID().toString

  def sendEvent[T](auditType: String, detail: T, transactionName: Option[String] = None)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext, fmt: Writes[T]): Future[AuditResult] = {

    val event = ExtendedDataEvent(
      auditSource = auditConnector.auditingConfig.auditSource,
      auditType   = auditType,
      eventId     = eventId(),
      tags        = hc.toAuditTags(
        transactionName = transactionName.getOrElse(auditType),
        path = hc.otherHeaders.collectFirst { case ("path", value) => value }.getOrElse("-")
      ),
      detail      = Json.toJson(detail),
      generatedAt = now()
    )

    auditConnector.sendExtendedEvent(event)
  }
}
