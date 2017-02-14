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
package api

import com.github.tomakehurst.wiremock.client.WireMock._
import itutil.{IntegrationSpecBase, WiremockHelper}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WS
import util.BasicBase64

class NotificationISpec extends IntegrationSpecBase {

  val mockHost = WiremockHelper.wiremockHost
  val mockPort = WiremockHelper.wiremockPort
  val mockUrl = s"http://$mockHost:$mockPort"

  val additionalConfiguration = Map(
    "auditing.consumer.baseUri.host" -> s"$mockHost",
    "auditing.consumer.baseUri.port" -> s"$mockPort",
    "microservice.services.auth.host" -> s"$mockHost",
    "microservice.services.auth.port" -> s"$mockPort",
    "microservice.services.company-registration.host" -> s"$mockHost",
    "microservice.services.company-registration.port" -> s"$mockPort",
    "basicAuthentication.enabled" -> "true",
    "basicAuthentication.realm" -> "test",
    "basicAuthentication.username" -> "foo",
    "basicAuthentication.password" -> "bar"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build

  private def client(path: String) = WS.url(s"http://localhost:$port/business-registration-notification$path").withFollowRedirects(false)

  class Setup {

  }

  private def getAuth(user: String, pwd: String) = {
    val enc = BasicBase64.encodeToString(s"${user}:${pwd}")
    s"""Basic ${enc}"""
  }

  "CT Notification" should {
    def setupSimpleAuthMocks() = {
      stubPost("/write/audit", 200, """{"x":2}""")
      stubGet("/auth/authority", 200, """{"uri":"xxx","credentials":{"gatewayId":"xxx2"},"userDetailsLink":"xxx3","ids":"/auth/ids"}""")
      stubGet("/auth/ids", 200, """{"internalId":"Int-xxx","externalId":"Ext-xxx"}""")
    }

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
          aResponse().
            withStatus(200).
            withBody(jsonCR)
        )
      )

      val response = client(s"/notification/BRCT0001").
        withHeaders("Authorization" -> getAuth("foo", "bar"), "Content-Type" -> "application/json").
        post(jsonBR).
        futureValue

      response.status shouldBe 200
      response.json  shouldBe Json.obj( "result" -> "ok", "timestamp" -> timestamp)
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