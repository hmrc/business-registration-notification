/*
 * Copyright 2020 HM Revenue & Customs
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

import config.Regimes
import models.ETMPNotification
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers._
import processors.{CTProcessor, PAYEProcessor}
import test.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class ServiceDirectorSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with Regimes {

  val mockPayeProcessor = mock[PAYEProcessor]
  val mockCTProcessor = mock[CTProcessor]

  implicit val hc = new HeaderCarrier()


  class Setup {
    val testDirector = new ServiceDirector(mockPayeProcessor, mockCTProcessor)
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

      when(mockCTProcessor.processRegime(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq(data))(ArgumentMatchers.any[HeaderCarrier]()))
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

      when(mockPayeProcessor.processRegime(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq(data))(ArgumentMatchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(OK))

      val result = await(testDirector.goToService("testAckRef", data.regime, data))
      result shouldBe OK
    }
  }
}
