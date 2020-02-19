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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, __}

case class ETMPNotification(timestamp : String,
                            regime : String,
                            taxId: Option[String],
                            status : String)

object ETMPNotification extends NotificationValidator {
  implicit val format = (
    (__ \ "timestamp").format[String](isoDateValidator) and
    (__ \ "regime").format[String](regimeValidator) and
    (__ \ "business-tax-identifier").formatNullable[String](taxIdentifierValidator) and
    (__ \ "status").format[String](statusValidator)
  )(ETMPNotification.apply, unlift(ETMPNotification.unapply))
}

case class CompanyRegistrationPost(ctUtr : Option[String],
                                   timestamp : String,
                                   status : String)

object CompanyRegistrationPost {
  implicit val format = Json.format[CompanyRegistrationPost]
}

case class PAYERegistrationPost(empRef: Option[String],
                                timestamp: String,
                                status: String)

object PAYERegistrationPost {
  implicit val format = Json.format[PAYERegistrationPost]
}
