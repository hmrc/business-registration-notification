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

package audit.events

import models.ETMPNotification
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ProcessedNotificationEventDetail(acknowledgementReference: String,
                                            timestamp: String,
                                            regime: String,
                                            ctUtr: Option[String],
                                            status: String)

object ProcessedNotificationEventDetail {

  def apply(ackRef: String, data: ETMPNotification): ProcessedNotificationEventDetail = {
    ProcessedNotificationEventDetail(
      ackRef,
      data.timestamp,
      data.regime,
      data.taxId,
      data.status
    )
  }

  implicit val writes: Writes[ProcessedNotificationEventDetail] =
    (detail: ProcessedNotificationEventDetail) => {

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
