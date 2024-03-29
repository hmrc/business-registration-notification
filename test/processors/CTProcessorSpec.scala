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

package processors

import audit.AuditService
import audit.events.ProcessedNotificationEventDetail
import models.{CompanyRegistrationPost, ETMPNotification}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import services.CompanyRegistrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CTProcessorSpec extends AnyWordSpec with should.Matchers with MockitoSugar {

  val mockAuditService: AuditService = mock[AuditService]
  val mockCompanyRegistrationService: CompanyRegistrationService = mock[CompanyRegistrationService]

  val testEtmpNotification: ETMPNotification =
    ETMPNotification(
      timestamp = "testStamp",
      regime = "corporation-tax",
      taxId = Some("testTaxId"),
      status = "testStatus"
    )

  val testEventDetailJson: JsValue =
    Json.parse(
      """
        |{
        |   "acknowledgementReference":"testAckRef",
        |   "timestamp":"testStamp",
        |   "regime":"corporation-tax",
        |   "ctUtr":"testTaxId",
        |   "status":"testStatus"
        |}""".stripMargin)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {
    val testProcessor = new CTProcessor(mockAuditService, mockCompanyRegistrationService)
  }

  "notificationToCRPost" should {
    "successfully convert an ETMP notification to a CompanyRegistrationPost" in new Setup {
      val result: CompanyRegistrationPost = testProcessor.notificationToCRPost(testEtmpNotification)
      result.ctUtr shouldBe Some("testTaxId")
      result.timestamp shouldBe "testStamp"
      result.status shouldBe "testStatus"
    }
  }

  "processRegime" should {
    "return an OK int" when {
      "the audit event fails but still sends the data to CR" in new Setup {

        when(mockAuditService.sendEvent(
          auditType = ArgumentMatchers.eq("taxRegistrationUpdateRequest"),
          detail = ArgumentMatchers.eq(ProcessedNotificationEventDetail("testAckRef", testEtmpNotification)),
          transactionName = ArgumentMatchers.any()
        )(
          hc = ArgumentMatchers.any[HeaderCarrier](),
          ec = ArgumentMatchers.any[ExecutionContext](),
          fmt = ArgumentMatchers.eq(ProcessedNotificationEventDetail.writes)
        ))
          .thenReturn(Future.failed(Failure("audit failed", Some(new Throwable))))

        when(mockCompanyRegistrationService.sendToCompanyRegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        val result: Int = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }

      "the audit event succeeds and sends the data to CR" in new Setup {
        when(mockAuditService.sendEvent(
          auditType = ArgumentMatchers.eq("taxRegistrationUpdateRequest"),
          detail = ArgumentMatchers.eq(ProcessedNotificationEventDetail("testAckRef", testEtmpNotification)),
          transactionName = ArgumentMatchers.any()
        )(
          hc = ArgumentMatchers.any[HeaderCarrier](),
          ec = ArgumentMatchers.any[ExecutionContext](),
          fmt = ArgumentMatchers.eq(ProcessedNotificationEventDetail.writes)
        ))
          .thenReturn(Future.successful(Success))

        when(mockCompanyRegistrationService.sendToCompanyRegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        val result: Int = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }
    }
  }
}
