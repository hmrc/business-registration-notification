/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{ETMPNotification, PAYERegistrationPost}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import services.CompanyRegistrationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PAYEProcessorSpec extends AnyWordSpec with should.Matchers with MockitoSugar {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val mockCompanyRegistrationService: CompanyRegistrationService = mock[CompanyRegistrationService]

  val testEtmpNotification: ETMPNotification =
    ETMPNotification(
      timestamp = "testStamp",
      regime = "paye",
      taxId = Some("testTaxId"),
      status = "testStatus"
    )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {
    val testProcessor = new PAYEProcessor(mockAuditConnector, mockCompanyRegistrationService)
  }

  "notificationToCRPost" should {
    "successfully convert an ETMP notification to a CompanyRegistrationPost" in new Setup {
      val result: PAYERegistrationPost = testProcessor.notificationToPAYEPost(testEtmpNotification)
      result.empRef shouldBe Some("testTaxId")
      result.timestamp shouldBe "testStamp"
      result.status shouldBe "testStatus"
    }
  }

  "processRegime" should {
    "return an OK int" when {
      "the data is sent to PR and audited successfully" in new Setup {
        when(mockCompanyRegistrationService.sendToPAYERegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        when(mockAuditConnector.sendExtendedEvent(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[ExecutionContext]()))
          .thenReturn(Future.successful(Success))

        val result: Int = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }
      "the data is sent to PR and auditing returns a Failure" in new Setup {
        when(mockCompanyRegistrationService.sendToPAYERegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        when(mockAuditConnector.sendExtendedEvent(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[ExecutionContext]()))
          .thenReturn(Future.failed(Failure("audit failed", Some(new Throwable))))

        val result: Int = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }
      "the data is sent to PR and auditing fails" in new Setup {
        when(mockCompanyRegistrationService.sendToPAYERegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        when(mockAuditConnector.sendExtendedEvent(ArgumentMatchers.any())(ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[ExecutionContext]()))
          .thenReturn(Future.failed(new RuntimeException))

        val result: Int = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }
    }
  }
}
