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

package util

import config.{MicroserviceAuditConnector, Regimes}
import models.ETMPNotification
import org.scalatest.mockito.MockitoSugar
import services.RegistrationService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import play.api.test.Helpers.OK
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class ServiceDirectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with Regimes {

  val mockCtService = mock[RegistrationService]
  val mockAuditConnector = mock[MicroserviceAuditConnector]

  implicit val hc = new HeaderCarrier()


  class Setup {
    val testDirector = new ServiceDirector(mockCtService, mockAuditConnector)
  }

  "goToService" should {
    "return a 400 if an invalid regime has been presented" in new Setup {
      val data = ETMPNotification(
        "testTimeStamp",
        "invalidRegime",
        Some("testID"),
        "testStatus"
      )

      val result = await(testDirector.goToService("testAckRef", data.regime, data))
      result shouldBe INVALID_REGIME
    }

    "return a 200 if corporation-tax is the regime and the data is sent to CR" in new Setup {
      val data = ETMPNotification(
        "testTimeStamp",
        "corporation-tax",
        Some("testID"),
        "testStatus"
      )

      when(mockCtService.sendToCompanyRegistration("testAckRef", ETMPNotification.convertToCRPost(data)))
        .thenReturn(Future.successful(OK))

      val result = await(testDirector.goToService("testAckRef", data.regime, data))
      result shouldBe OK
    }

    "return a 200 if paye is the regime and the data is sent to PR" in new Setup {
      val data = ETMPNotification(
        "testTimeStamp",
        "paye",
        Some("testID"),
        "testStatus"
      )

      when(mockCtService.sendToPAYERegistration("testAckRef", ETMPNotification.convertToPRPost(data)))
        .thenReturn(Future.successful(OK))

      val result = await(testDirector.goToService("testAckRef", data.regime, data))
      result shouldBe OK
    }
  }
}
