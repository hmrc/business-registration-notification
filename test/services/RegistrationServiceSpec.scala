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

package services

import config.WSHttp
import connectors.{CompanyRegistrationConnector, PAYERegistrationConnector}
import mocks.MockHttp
import models.{CompanyRegistrationPost, PAYERegistrationPost}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers.OK
import org.mockito.Mockito._
import org.mockito.Matchers
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class RegistrationServiceSpec extends UnitSpec with WithFakeApplication with MockitoSugar with MockHttp {

  val mockCRConnector = mock[CompanyRegistrationConnector]
  val mockPAYEConnector = mock[PAYERegistrationConnector]
  val mockWSHttp = mock[WSHttp]

  val response = mockHttpResponse(OK)

  implicit val hc = new HeaderCarrier()

  class Setup {
    val testService = new RegistrationSrv {
      override val crConnector = mockCRConnector
      override val payeConnector = mockPAYEConnector
    }
  }

  "sendToCompanyRegistration" should {
    "return an int" in new Setup {
      val data = CompanyRegistrationPost(
        Some("testUtr"),
        "testTimeStamp",
        "testStatus"
      )

      when(mockCRConnector.processAcknowledgment(Matchers.eq("testAckRef"), Matchers.eq(data))(Matchers.any()))
        .thenReturn(Future.successful(response))

      val result = await(testService.sendToCompanyRegistration("testAckRef", data))
      result shouldBe OK
    }
  }

  "sendToPAYERegistration" should {
    "return an int" in new Setup {
      val data = PAYERegistrationPost(
        Some("testEMPREF"),
        "testTimeStamp",
        "testStatus"
      )

      when(mockPAYEConnector.processAcknowledgement(Matchers.eq("testAckRef"), Matchers.eq(data))(Matchers.any()))
        .thenReturn(Future.successful(response))

      val result = await(testService.sendToPAYERegistration("testAckRef", data))
      result shouldBe OK
    }
  }
}
