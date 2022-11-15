/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.{CompanyRegistrationConnector, PAYERegistrationConnector}
import mocks.MockHttp
import models.{CompanyRegistrationPost, PAYERegistrationPost}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CompanyRegistrationServiceSpec extends AnyWordSpec with should.Matchers with MockitoSugar with MockHttp {

  val mockCRConnector: CompanyRegistrationConnector = mock[CompanyRegistrationConnector]
  val mockPAYEConnector: PAYERegistrationConnector = mock[PAYERegistrationConnector]

  val response: HttpResponse = mockHttpResponse(OK)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {
    val testService = new CompanyRegistrationService(mockCRConnector, mockPAYEConnector)
  }

  "sendToCompanyRegistration" should {
    "return an int" in new Setup {
      val data: CompanyRegistrationPost = CompanyRegistrationPost(
        Some("testUtr"),
        "testTimeStamp",
        "testStatus"
      )

      when(mockCRConnector.processAcknowledgment(
        ArgumentMatchers.eq("testAckRef"),
        ArgumentMatchers.eq(data)
      )(ArgumentMatchers.any(),
        ArgumentMatchers.any[ExecutionContext])).thenReturn(Future.successful(response))

      val result: Int = await(testService.sendToCompanyRegistration("testAckRef", data))
      result shouldBe OK
    }
  }

  "sendToPAYERegistration" should {
    "return an int" in new Setup {
      val data: PAYERegistrationPost = PAYERegistrationPost(
        Some("testEMPREF"),
        "testTimeStamp",
        "testStatus"
      )

      when(mockPAYEConnector.processAcknowledgement(
        ArgumentMatchers.eq("testAckRef"),
        ArgumentMatchers.eq(data)
      )(ArgumentMatchers.any())).thenReturn(Future.successful(response))

      val result: Int = await(testService.sendToPAYERegistration("testAckRef", data))
      result shouldBe OK
    }
  }
}
