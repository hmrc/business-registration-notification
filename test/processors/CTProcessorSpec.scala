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

import config.MicroserviceAuditConnector
import models.ETMPNotification
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers
import play.api.libs.json.Json
import services.RegistrationService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers.OK
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Failure, Success}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

class CTProcessorSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockAuditConnector = mock[MicroserviceAuditConnector]
  val mockRegistratioService = mock[RegistrationService]

  val testEtmpNotification =
    ETMPNotification(
      timestamp = "testStamp",
      regime = "corporation-tax",
      taxId = Some("testTaxId"),
      status = "testStatus"
    )

  val testEventDetailJson =
    Json.parse(
      """
        |{
        |   "acknowledgementReference":"testAckRef",
        |   "timestamp":"testStamp",
        |   "regime":"corporation-tax",
        |   "ctUtr":"testTaxId",
        |   "status":"testStatus"
        |}""".stripMargin)

  implicit val hc = HeaderCarrier()

  class Setup {
    val testProcessor = new CTProcessor(mockAuditConnector, mockRegistratioService)
  }

  "notificationToCRPost" should {
    "successfully convert an ETMP notification to a CompanyRegistrationPost" in new Setup {
      val result = testProcessor.notificationToCRPost(testEtmpNotification)
      result.ctUtr shouldBe Some("testTaxId")
      result.timestamp shouldBe "testStamp"
      result.status shouldBe "testStatus"
    }
  }

  "processRegime" should {
    "return an OK int" when {
      "the audit event fails but still sends the data to CR" in new Setup {

        when(mockAuditConnector.sendExtendedEvent(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[ExecutionContext]()))
          .thenReturn(Future.failed(Failure("audit failed", Some(new Throwable))))

        when(mockRegistratioService.sendToCompanyRegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        val result = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }

      "the audit event succeeds and sends the data to CR" in new Setup {
        when(mockAuditConnector.sendExtendedEvent(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[ExecutionContext]()))
          .thenReturn(Future.successful(Success))

        when(mockRegistratioService.sendToCompanyRegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        val result = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }
    }
  }
}
