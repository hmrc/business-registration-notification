/*
 * Copyright 2020 HM Revenue & Customs
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

package audit.events

import play.api.libs.functional.syntax._
import play.api.libs.json.{Writes, _}
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

case class ProcessedNotificationEventDetail(acknowledgementReference: String,
                                            timestamp: String,
                                            regime: String,
                                            ctUtr: Option[String],
                                            status: String)

object ProcessedNotificationEventDetail {
  implicit val writes = new Writes[ProcessedNotificationEventDetail] {
    def writes(detail: ProcessedNotificationEventDetail): JsObject = {

      val taxIdentifier = detail.regime match {
        case "paye" => "empRef"
        case _ => "ctUtr"
      }

      val notificationWrites = (
        (__ \ "acknowledgementReference").write[String] and
          (__ \ "timestamp").write[String] and
          (__ \ "regime").write[String] and
          (__ \ taxIdentifier).writeNullable[String] and
          (__ \ "status").write[String]
        ) (unlift(ProcessedNotificationEventDetail.unapply))

      Json.toJson(detail)(notificationWrites).as[JsObject]
    }
  }
}

class ProcessedNotificationEvent(auditRef: String, details: ProcessedNotificationEventDetail, transactionName: Option[String] = None)
  extends RegistrationAuditEvent(auditRef, Json.toJson(details).as[JsObject], transactionName)

object ProcessedNotificationEvent {
  implicit val format = Json.format[ExtendedDataEvent]
}
