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

import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec

class ETMPNotificationSpec extends UnitSpec with JsonFormatValidation {

  def lineEnd(comma: Boolean) = if( comma ) "," else ""
  def jsonLine(key: String, value: String): String = jsonLine(key, value, true)
  def jsonLine(key: String, value: String, comma: Boolean): String = s""""${key}" : "${value}"${lineEnd(comma)}"""
  def jsonLine(key: String, value: Option[String], comma: Boolean = true): String = value.fold("")(v=>s""""${key}" : "${v}"${lineEnd(comma)}""")

  val defaultModel = ETMPNotification("2001-12-31T12:00:00Z", "corporation-tax", Some("123456789"), "04")
  def j(utr: Option[String] = defaultModel.taxId, status: String = defaultModel.status, regime: String = defaultModel.regime) = {
    s"""
       |{
       |  "timestamp": "${defaultModel.timestamp}",
       |  "regime": "${regime}",
       |  ${jsonLine("business-tax-identifier", utr)}
       |  "status": "${status}"
       |}
     """.stripMargin
  }

  "ETMPNotification Model - utr" should {
    "Be able to be parsed with valid UTR" in {
      val utr = Some("123456789012345")
      val json = j(utr = utr)
      val expected = defaultModel.copy(taxId = utr)

      val result = Json.parse(json).validate[ETMPNotification]

      shouldBeSuccess(expected, result)
    }

    "Be able to be parsed without UTR" in {
      val json = j(utr = None)
      val expected = defaultModel.copy(taxId = None)

      val result = Json.parse(json).validate[ETMPNotification]

      shouldBeSuccess(expected, result)
    }

    "fail to be read from JSON if is empty string" in {
      val json = j(utr=Some(""))

      val result = Json.parse(json).validate[ETMPNotification]

      shouldHaveErrors(result, JsPath() \ "business-tax-identifier", Seq(ValidationError("error.minLength", 1)))
    }

    "fail to be read from JSON if line1 is longer than 27 characters" in {
      val json = j(utr=Some("1234567890123456"))

      val result = Json.parse(json).validate[ETMPNotification]

      shouldHaveErrors(result, JsPath() \ "business-tax-identifier", Seq(ValidationError("error.maxLength", 15)))
    }
  }

  "ETMPNotification Model - regime" should {
    "Be able to be parsed with valid regime" in {
      val json = j()
      val expected = defaultModel

      val result = Json.parse(json).validate[ETMPNotification]

      shouldBeSuccess(expected, result)
    }

    "fail to be read from JSON if the regime is wrong" in {
      val json = j(regime = "123456789012345")

      val result = Json.parse(json).validate[ETMPNotification]

      shouldHaveErrors(result, JsPath() \ "regime", Seq(ValidationError("error.pattern")))
    }
  }

}
