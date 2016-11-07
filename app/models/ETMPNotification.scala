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

package models

import play.api.libs.json.{Json, __}
import play.api.libs.functional.syntax._

case class ETMPNotification(timestamp : String,
                            regime : String,
                            taxId: Option[String],
                            status : String)

object ETMPNotification {
  implicit val format = (
    (__ \ "timestamp").format[String] and
    (__ \ "regime").format[String] and
    (__ \ "business-tax-identifier").formatNullable[String] and
    (__ \ "status").format[String]
  )(ETMPNotification.apply, unlift(ETMPNotification.unapply))

  def convertToCRPost(eTMPNotification: ETMPNotification) : CompanyRegistrationPost = {
    CompanyRegistrationPost(
      eTMPNotification.taxId,
      eTMPNotification.timestamp,
      eTMPNotification.status
    )
  }
}

case class CompanyRegistrationPost(ctUtr : Option[String],
                                   timestamp : String,
                                   status : String)

object CompanyRegistrationPost {
  implicit val format = Json.format[CompanyRegistrationPost]
}