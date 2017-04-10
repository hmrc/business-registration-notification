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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers.OK
import services.RegistrationService
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.{ExecutionContext, Future}

class PAYEProcessorSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockAuditConnector = mock[MicroserviceAuditConnector]
  val mockRegistratioService = mock[RegistrationService]

  val testEtmpNotification =
    ETMPNotification(
      timestamp = "testStamp",
      regime = "paye",
      taxId = Some("testTaxId"),
      status = "testStatus"
    )

  implicit val hc = HeaderCarrier()

  class Setup {
    val testProcessor = new PAYEProcessor(mockAuditConnector, mockRegistratioService)
  }

  "notificationToCRPost" should {
    "successfully convert an ETMP notification to a CompanyRegistrationPost" in new Setup {
      val result = testProcessor.notificationToPAYEPost(testEtmpNotification)
      result.empRef shouldBe Some("testTaxId")
      result.timestamp shouldBe "testStamp"
      result.status shouldBe "testStatus"
    }
  }

  "processRegime" should {
    "return an OK int" when {
      "the data is end PR" in new Setup {
        when(mockRegistratioService.sendToPAYERegistration(ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        val result = await(testProcessor.processRegime("testAckRef", testEtmpNotification))
        result shouldBe OK
      }
    }
  }
}