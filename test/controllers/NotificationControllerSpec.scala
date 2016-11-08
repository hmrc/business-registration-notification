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

package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import models.ETMPNotification
import org.scalatest.mock.MockitoSugar
import services.CompanyRegistrationService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.Matchers
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.HeaderCarrier
import util.ServiceDirector
import play.api.test.Helpers._

import scala.concurrent.Future

class NotificationControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockDirector = mock[ServiceDirector]

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val hc = new HeaderCarrier()

  val data = ETMPNotification(
    "2001-12-31T12:00:00Z",
    "corporation-tax",
    Some("testUTRRRRRRRRRRRRRRRRRRRRRR"),
    "04"
  )

  class Setup {
    object TestController extends NotificationController {
      val director = mockDirector
    }
  }

  "NotificationController" should {
    "use the correct service" in {
      NotificationController.director shouldBe ServiceDirector
    }
  }

  "processNotification" should {
    "return an BADREQUEST" when {
      "the UTR is too long" in new Setup {

        val request = FakeRequest().withBody(Json.toJson(data))

        when(mockDirector.goToService(Matchers.eq("testAckRef"), Matchers.eq("corporation-tax"), Matchers.eq(data))(Matchers.any()))
          .thenReturn(Future.successful(OK))

        val result = await(TestController.processNotification("testAckRef")(request))
        status(result) shouldBe BAD_REQUEST
      }

      "the UTR isnt present" in new Setup {

        val request = FakeRequest().withBody(Json.toJson(data.copy(taxId = Some(""))))

        when(mockDirector.goToService(Matchers.eq("testAckRef"), Matchers.eq("corporation-tax"), Matchers.eq(data.copy(taxId = Some(""))))(Matchers.any()))
          .thenReturn(Future.successful(OK))

        val result = await(TestController.processNotification("testAckRef")(request))
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return an OK" when {
      "a record has been successfully updated" in new Setup {

        val request = FakeRequest().withBody(Json.toJson(data.copy(taxId = Some("123456789"))))

        when(mockDirector.goToService(Matchers.eq("testAckRef"), Matchers.eq("corporation-tax"), Matchers.eq(data.copy(taxId = Some("123456789"))))(Matchers.any()))
          .thenReturn(Future.successful(OK))

        val result = await(TestController.processNotification("testAckRef")(request))
        status(result) shouldBe OK
      }
    }
  }
}
