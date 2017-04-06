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

package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import basicauth.{BasicAuthenticatedAction, BasicAuthenticationFilterConfiguration}
import com.codahale.metrics.Counter
import models.ETMPNotification
import org.scalatest.mockito.MockitoSugar
import services.MetricsService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.play.http.{HeaderCarrier, InternalServerException, NotFoundException, ServiceUnavailableException}
import util.ServiceDir
import play.api.test.Helpers._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.Future

class NotificationControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val mockDirector = mock[ServiceDir]
  val mockMetrics = mock[MetricsService]
  val mockAuditConnector = mock[AuditConnector]
  val mockConf = mock[Configuration]

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val hc = new HeaderCarrier()

  val data = ETMPNotification(
    "2001-12-31T12:00:00Z",
    "corporation-tax",
    Some("testUTRRRRRRRRRRRRRRRRRRRRRR"),
    "04"
  )



  val bafc = new BasicAuthenticationFilterConfiguration("1234", false, "username", "password")
  val mockAuthAction = new BasicAuthenticatedAction(bafc)


  class Setup {
    object TestController extends NotificationCtrl {
      val director = mockDirector
      val metrics = new MetricsService {
        private val mockCounter = mock[Counter]
        override val serviceNotAvailable: Counter = mockCounter
        override val clientErrorCodes: Counter = mockCounter
        override val etmpNotificationCounter: Counter = mockCounter
        override val ackRefNotFound: Counter = mockCounter
        override val internalServerError: Counter = mockCounter
        override val serverErrorCodes: Counter = mockCounter
      }
      val authAction = mockAuthAction
      val config = mockConf
      val auditConnector = mockAuditConnector

    }
  }



  "processNotification" should {
    "return an BADREQUEST" when {
      "the UTR is too long" in new Setup {

        when(mockDirector.goToService(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())(ArgumentMatchers.any()))
        .thenReturn(Future.successful(OK))

        val request = FakeRequest().withHeaders("Authorization" -> "Basic Zm9vOmJhcg==").withBody(Json.toJson(data))
        val result = await(TestController.processNotification("testAckRef")(request))
        status(result) shouldBe BAD_REQUEST
      }

      "the UTR isnt present" in new Setup {

        val request = FakeRequest().withHeaders("Authorization" -> "Basic Zm9vOmJhcg==").withBody(Json.toJson(data.copy(taxId = Some(""))))

        when(mockDirector.goToService(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq("corporation-tax"), ArgumentMatchers.eq(data.copy(taxId = Some(""))))(ArgumentMatchers.any()))
          .thenReturn(Future.successful(OK))

        val result = await(TestController.processNotification("testAckRef")(request))
        status(result) shouldBe BAD_REQUEST
      }
    }

          "return an OK" when {
            "a record has been successfully updated" in new Setup {

              val request = FakeRequest().withHeaders("Authorization" -> "Basic Zm9vOmJhcg==").withBody(Json.toJson(data.copy(taxId = Some("123456789"))))

              when(mockDirector
                .goToService(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq("corporation-tax"), ArgumentMatchers.eq(data.copy(taxId = Some("123456789"))))(ArgumentMatchers.any()))
                .thenReturn(Future.successful(OK))

              val result = await(TestController.processNotification("testAckRef")(request))
              status(result) shouldBe OK
            }
          }

          "return an other status" in new Setup {
            val request = FakeRequest().withHeaders("Authorization" -> "Basic Zm9vOmJhcg==").withBody(Json.toJson(data.copy(taxId = Some("123456789"))))

            when(mockDirector
              .goToService(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq("corporation-tax"), ArgumentMatchers.eq(data.copy(taxId = Some("123456789"))))(ArgumentMatchers.any()))
              .thenReturn(Future.successful(CONTINUE))

            val result = await(TestController.processNotification("testAckRef")(request))
            status(result) shouldBe CONTINUE
          }

          "return a NotFound" in new Setup {
            val request = FakeRequest().withHeaders("Authorization" -> "Basic Zm9vOmJhcg==").withBody(Json.toJson(data.copy(taxId = Some("123456789"))))

            when(mockDirector
              .goToService(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq("corporation-tax"), ArgumentMatchers.eq(data.copy(taxId = Some("123456789"))))(ArgumentMatchers.any()))
              .thenReturn(Future.failed(new NotFoundException("")))

            val result = await(TestController.processNotification("testAckRef")(request))
            status(result) shouldBe NOT_FOUND
          }

          "return a ServiceUnavailable" in new Setup {
            val request = FakeRequest().withHeaders("Authorization" -> "Basic Zm9vOmJhcg==").withBody(Json.toJson(data.copy(taxId = Some("123456789"))))

            when(mockDirector
              .goToService(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq("corporation-tax"), ArgumentMatchers.eq(data.copy(taxId = Some("123456789"))))(ArgumentMatchers.any()))
              .thenReturn(Future.failed(new ServiceUnavailableException("")))

            val result = await(TestController.processNotification("testAckRef")(request))
            status(result) shouldBe SERVICE_UNAVAILABLE
          }

          "return a InternalServerError" in new Setup {
            val request = FakeRequest().withHeaders("Authorization" -> "Basic Zm9vOmJhcg==").withBody(Json.toJson(data.copy(taxId = Some("123456789"))))

            when(mockDirector
              .goToService(ArgumentMatchers.eq("testAckRef"), ArgumentMatchers.eq("corporation-tax"), ArgumentMatchers.eq(data.copy(taxId = Some("123456789"))))(ArgumentMatchers.any()))
              .thenReturn(Future.failed(new InternalServerException("")))

            val result = await(TestController.processNotification("testAckRef")(request))
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
    }
}
