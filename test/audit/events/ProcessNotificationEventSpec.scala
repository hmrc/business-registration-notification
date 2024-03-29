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

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsObject, Json}

class ProcessNotificationEventSpec extends AnyWordSpec with should.Matchers {

  "ProcessedNotificationEvent" should {
    "construct a valid json structure as per confluence" when {
      "converting a case class to Json with a CTUTR" in {
        val expected: String =
          """
            |{
            |   "acknowledgementReference" : "BRCT123456789",
            |   "timestamp" : "2001-12-31T12:00:00Z",
            |   "regime" : "corporation-tax",
            |   "ctUtr" : "1234567890",
            |   "status" : "04"
            |}
          """.stripMargin

        val testModel = ProcessedNotificationEventDetail(
          "BRCT123456789",
          "2001-12-31T12:00:00Z",
          "corporation-tax",
          Some("1234567890"),
          "04")

        val result = Json.toJson[ProcessedNotificationEventDetail](testModel)
        result.getClass shouldBe classOf[JsObject]
        result shouldBe Json.parse(expected)
      }

      "converting a case class to Json without a CTUTR" in {
        val expected: String =
          """
            |{
            |   "acknowledgementReference" : "BRCT123456789",
            |   "timestamp" : "2001-12-31T12:00:00Z",
            |   "regime" : "corporation-tax",
            |   "status" : "04"
            |}
          """.stripMargin

        val testModel = ProcessedNotificationEventDetail(
          "BRCT123456789",
          "2001-12-31T12:00:00Z",
          "corporation-tax",
          None,
          "04")

        val result = Json.toJson[ProcessedNotificationEventDetail](testModel)
        result.getClass shouldBe classOf[JsObject]
        result shouldBe Json.parse(expected)
      }
    }
  }
}
