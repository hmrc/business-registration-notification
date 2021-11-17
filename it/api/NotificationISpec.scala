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

package api

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import itutil.{IntegrationSpecBase, WiremockHelper}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.Helpers._
import util.BasicBase64

class NotificationISpec extends IntegrationSpecBase with MockitoSugar {

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: Int = WiremockHelper.wiremockPort
  val mockUrl: String = s"http://$mockHost:$mockPort"

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build

  def additionalConfiguration = Map(
    "auditing.enabled" -> "true",
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> s"$mockPort",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> s"$mockPort",
    "microservice.services.company-registration.host" -> mockHost,
    "microservice.services.company-registration.port" -> s"$mockPort",
    "microservice.services.paye-registration.host" -> mockHost,
    "microservice.services.paye-registration.port" -> s"$mockPort",
    "basicAuthentication.enabled" -> "true",
    "basicAuthentication.realm" -> "test",
    "basicAuthentication.username" -> "testUser",
    "basicAuthentication.password" -> "password",
  )

  lazy val ws: WSClient = app.injector.instanceOf[WSClient]

  private def client(path: String): WSRequest = ws.url(s"http://localhost:$port/business-registration-notification$path").withFollowRedirects(false)

  private val getAuth: String = s"Basic ${BasicBase64.encodeToString("testUser:password")}"

  "Notification" should {
    def setupSimpleAuthMocks(): StubMapping = {
      stubPost("/write/audit", OK, """{"x":2}""")
      stubPost("/write/audit/merged", OK, """{"x":2}""")
      stubGet("/auth/authority", OK, """{"uri":"xxx","credentials":{"gatewayId":"xxx2"},"userDetailsLink":"xxx3","ids":"/auth/ids"}""")
      stubGet("/auth/ids", OK, """{"internalId":"Int-xxx","externalId":"Ext-xxx"}""")
    }

    def setupFailedAuditing(): StubMapping = stubPost("/write/audit", INTERNAL_SERVER_ERROR, """{"message":"Test Error"}""")

    "call CR and return a success" in {
      setupSimpleAuthMocks()

      val timestamp = "2017-01-01T00:00:00"
      val jsonBR = s"""{"regime":"corporation-tax", "timestamp":"$timestamp", "status":"04"}"""
      val jsonCR = s"""{"timestamp":"$timestamp", "status":"04"}"""

      stubFor(
        post(urlMatching("/company-registration/corporation-tax-registration/acknowledgement-confirmation?(.*)"))
          .withQueryParam("ackref", equalTo("BRCT0001"))
          .withRequestBody(equalToJson(jsonCR))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(jsonCR))
      )

      lazy val response: WSResponse = await(
        client(s"/notification/BRCT0001")
          .withHttpHeaders("Authorization" -> getAuth, "Content-Type" -> "application/json")
          .post(jsonBR)
      )

      response.status shouldBe 200
      response.json shouldBe Json.obj("result" -> "ok", "timestamp" -> timestamp)
    }

    "call PR and return a success on successful ETMP registration" in {
      setupSimpleAuthMocks()

      val timestamp = "2017-01-01T00:00:00"
      val jsonBR = s"""{"timestamp":"$timestamp", "regime":"paye", "status":"04", "business-tax-identifier":"EMPREF0001"}"""
      val jsonPR = s"""{"timestamp":"$timestamp", "status":"04", "empRef":"EMPREF0001"}"""

      stubFor(
        post(urlMatching("/paye-registration/registration-processed-confirmation?(.*)"))
          .withQueryParam("ackref", equalTo("BRPY0001"))
          .withRequestBody(equalToJson(jsonPR))
          .willReturn(
            aResponse().
              withStatus(OK).
              withBody(jsonPR)
          )
      )

      lazy val response: WSResponse = await(
        client(s"/notification/BRPY0001")
          .withHttpHeaders("Authorization" -> getAuth, "Content-Type" -> "application/json")
          .post(jsonBR)
      )

      response.status shouldBe OK
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

    "call PR and return a success on ETMP rejection" in {
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
              withStatus(OK).
              withBody(jsonPR)
          )
      )

      lazy val response: WSResponse = await(
        client(s"/notification/BRPY0002")
          .withHttpHeaders("Authorization" -> getAuth, "Content-Type" -> "application/json")
          .post(jsonBR)
      )

      response.status shouldBe OK
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

    "handle auditing errors" in {
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
              withStatus(OK).
              withBody(jsonPR)
          )
      )

      lazy val response: WSResponse = await(
        client(s"/notification/BRPY0002")
          .withHttpHeaders("Authorization" -> getAuth, "Content-Type" -> "application/json")
          .post(jsonBR)
      )

      response.status shouldBe OK
      response.json shouldBe Json.obj("result" -> "ok", "timestamp" -> timestamp)

      verify(1, postRequestedFor(urlMatching("/paye-registration/registration-processed-confirmation\\?ackref=BRPY0002")))
    }

    "handle downstream errors" in {
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
              withStatus(BAD_REQUEST).
              withBody(jsonPR)
          )
      )

      lazy val response: WSResponse = await(
        client(s"/notification/BRPY0002")
          .withHttpHeaders("Authorization" -> getAuth, "Content-Type" -> "application/json")
          .post(jsonBR)
      )

      response.status shouldBe INTERNAL_SERVER_ERROR

      verify(1, postRequestedFor(urlMatching("/paye-registration/registration-processed-confirmation\\?ackref=BRPY0002")))
    }
  }

  "return a 401 if the creds are not valid" in {

    val getAuth: String = s"Basic ${BasicBase64.encodeToString("testUser1:password1")}"

    lazy val response = await(
      client(s"/notification/BRCT0001")
        .withHttpHeaders("Authorization" -> getAuth, "Content-Type" -> "application/json")
        .post("{}")
    )

    response.status shouldBe 401
  }
}
