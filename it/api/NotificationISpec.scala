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
package api

import com.github.tomakehurst.wiremock.client.WireMock._
import itutil.{IntegrationSpecBase, WiremockHelper}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import util.BasicBase64

class NotificationISpec extends IntegrationSpecBase with MockitoSugar {

  lazy val ws = app.injector.instanceOf[WSClient]

  val mockHost = WiremockHelper.wiremockHost
  val mockPort = WiremockHelper.wiremockPort
  val mockUrl = s"http://$mockHost:$mockPort"

  val additionalConfiguration = Map(
    "Test.auditing.consumer.baseUri.host" -> s"$mockHost",
    "Test.auditing.consumer.baseUri.port" -> s"$mockPort",
    "microservice.services.auth.host" -> s"$mockHost",
    "microservice.services.auth.port" -> s"$mockPort",
    "microservice.services.company-registration.host" -> s"$mockHost",
    "microservice.services.company-registration.port" -> s"$mockPort",
    "microservice.services.paye-registration.host" -> s"$mockHost",
    "microservice.services.paye-registration.port" -> s"$mockPort",
    "basicAuthentication.enabled" -> "true",
    "basicAuthentication.realm" -> "test",
    "basicAuthentication.username" -> "foo",
    "basicAuthentication.password" -> "bar"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build

  private def client(path: String) = ws.url(s"http://localhost:$port/business-registration-notification$path").withFollowRedirects(false)

  class Setup {

  }

  private def getAuth(user: String, pwd: String) = {
    val enc = BasicBase64.encodeToString(s"${user}:${pwd}")
    s"""Basic ${enc}"""
  }

  "Notification" should {
    def setupSimpleAuthMocks() = {
      stubPost("/write/audit", 200, """{"x":2}""")
      stubGet("/auth/authority", 200, """{"uri":"xxx","credentials":{"gatewayId":"xxx2"},"userDetailsLink":"xxx3","ids":"/auth/ids"}""")
      stubGet("/auth/ids", 200, """{"internalId":"Int-xxx","externalId":"Ext-xxx"}""")
    }

    def setupFailedAuditing() = stubPost("/write/audit", 500, """{"message":"Test Error"}""")

    "call CR and return a success" in new Setup {
      setupSimpleAuthMocks()

      val timestamp = "2017-01-01T00:00:00"
      val jsonBR = s"""{"regime":"corporation-tax", "timestamp":"${timestamp}", "status":"04"}"""
      val jsonCR = s"""{"timestamp":"${timestamp}", "status":"04"}"""

      stubFor(
        post(urlMatching("/company-registration/corporation-tax-registration/acknowledgement-confirmation?(.*)"))
          .withQueryParam("ackref", equalTo("BRCT0001"))
          .withRequestBody(equalToJson(jsonCR))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(jsonCR))
      )

      val response = client(s"/notification/BRCT0001").
        withHeaders("Authorization" -> getAuth("foo", "bar"), "Content-Type" -> "application/json").
        post(jsonBR).
        futureValue

      response.status shouldBe 200
      response.json shouldBe Json.obj("result" -> "ok", "timestamp" -> timestamp)
    }

    "call PR and return a success on successful ETMP registration" in new Setup {
      setupSimpleAuthMocks()

      val timestamp = "2017-01-01T00:00:00"
      val jsonBR = s"""{"timestamp":"${timestamp}", "regime":"paye", "status":"04", "business-tax-identifier":"EMPREF0001"}"""
      val jsonPR = s"""{"timestamp":"${timestamp}", "status":"04", "empRef":"EMPREF0001"}"""

      stubFor(
        post(urlMatching("/paye-registration/registration-processed-confirmation?(.*)"))
          .withQueryParam("ackref", equalTo("BRPY0001"))
          .withRequestBody(equalToJson(jsonPR))
          .willReturn(
            aResponse().
              withStatus(200).
              withBody(jsonPR)
          )
      )

      val response = client(s"/notification/BRPY0001").
        withHeaders("Authorization" -> getAuth("foo", "bar"), "Content-Type" -> "application/json").
        post(jsonBR).
        futureValue

      response.status shouldBe 200
      response.json shouldBe Json.obj("result" -> "ok", "timestamp" -> timestamp)

      verify(postRequestedFor(urlMatching("/write/audit"))
        .withRequestBody(equalToJson(Json.parse(
          s"""
             |{
             |  "auditSource" : "business-registration-notification",
             |  "auditType" : "successfulTaxServiceRegistration",
             |  "detail" : {
             |    "acknowledgementReference" : "BRPY0001",
             |    "timestamp" : "$timestamp",
             |    "regime" : "paye",
             |    "empRef" : "EMPREF0001",
             |    "status" : "04"
             |  },
             |  "tags" : {
             |    "transactionName" : "payeRegistrationUpdateRequest"
             |  }
             |}
          """.stripMargin).toString(), true, true)
        )
      )
    }

    "call PR and return a success on ETMP rejection" in new Setup {
      setupSimpleAuthMocks()

      val timestamp = "2017-01-01T00:00:00"
      val jsonBR = s"""{"timestamp":"$timestamp", "regime":"paye", "status":"07"}"""
      val jsonPR = s"""{"timestamp":"$timestamp", "status":"07"}"""

      stubFor(
        post(urlMatching("/paye-registration/registration-processed-confirmation?(.*)"))
          .withQueryParam("ackref", equalTo("BRPY0002"))
          .withRequestBody(equalToJson(jsonPR))
          .willReturn(
            aResponse().
              withStatus(200).
              withBody(jsonPR)
          )
      )

      val response = client(s"/notification/BRPY0002").
        withHeaders("Authorization" -> getAuth("foo", "bar"), "Content-Type" -> "application/json").
        post(jsonBR).
        futureValue

      response.status shouldBe 200
      response.json shouldBe Json.obj("result" -> "ok", "timestamp" -> timestamp)

      verify(postRequestedFor(urlMatching("/write/audit"))
        .withRequestBody(equalToJson(Json.parse(
          s"""
             |{
             |  "auditSource" : "business-registration-notification",
             |  "auditType" : "rejectedTaxServiceRegistration",
             |  "detail" : {
             |    "acknowledgementReference" : "BRPY0002",
             |    "timestamp" : "$timestamp",
             |    "regime" : "paye",
             |    "status" : "07"
             |  },
             |  "tags" : {
             |    "transactionName" : "payeRegistrationUpdateRequest"
             |  }
             |}
          """.stripMargin).toString(), true, true)
        )
      )
    }

    "handle auditing errors" in new Setup {
      setupSimpleAuthMocks()
      setupFailedAuditing()

      val timestamp = "2017-01-01T00:00:00"
      val jsonBR = s"""{"timestamp":"$timestamp", "regime":"paye", "status":"07"}"""
      val jsonPR = s"""{"timestamp":"$timestamp", "status":"07"}"""

      stubFor(
        post(urlMatching("/paye-registration/registration-processed-confirmation?(.*)"))
          .withQueryParam("ackref", equalTo("BRPY0002"))
          .withRequestBody(equalToJson(jsonPR))
          .willReturn(
            aResponse().
              withStatus(200).
              withBody(jsonPR)
          )
      )

      val response = client(s"/notification/BRPY0002").
        withHeaders("Authorization" -> getAuth("foo", "bar"), "Content-Type" -> "application/json").
        post(jsonBR).
        futureValue

      verify(1, postRequestedFor(urlMatching("/paye-registration/registration-processed-confirmation\\?ackref=BRPY0002")))

      response.status shouldBe 200
      response.json shouldBe Json.obj("result" -> "ok", "timestamp" -> timestamp)
    }

    "handle downstream errors" in new Setup {
      setupSimpleAuthMocks()

      val timestamp = "2017-01-01T00:00:00"
      val jsonBR = s"""{"timestamp":"$timestamp", "regime":"paye", "status":"07"}"""
      val jsonPR = s"""{"timestamp":"$timestamp", "status":"07"}"""

      stubFor(
        post(urlMatching("/paye-registration/registration-processed-confirmation?(.*)"))
          .withQueryParam("ackref", equalTo("BRPY0002"))
          .withRequestBody(equalToJson(jsonPR))
          .willReturn(
            aResponse().
              withStatus(400).
              withBody(jsonPR)
          )
      )

      val response = client(s"/notification/BRPY0002").
        withHeaders("Authorization" -> getAuth("foo", "bar"), "Content-Type" -> "application/json").
        post(jsonBR).
        futureValue

      verify(1, postRequestedFor(urlMatching("/paye-registration/registration-processed-confirmation\\?ackref=BRPY0002")))

      response.status shouldBe 500
    }
  }

  "return a 401 if the creds are not valid" in {
    val response = client(s"/notification/BRCT0001").
      withHeaders("Authorization" -> getAuth("foo", "bar2"), "Content-Type" -> "application/json").
      post("{}").
      futureValue

    response.status shouldBe 401
  }
}
