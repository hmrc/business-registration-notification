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

package controllers

import basicauth.{BasicAuthenticatedAction, BasicAuthenticationFilterConfiguration}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Configuration}
import test.UnitSpec
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class PingSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  val testUserName = "foo"
  val testPassword = "bar"

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build

  val additionalConfiguration: Map[String, String] = Map(
    "Test.basicAuthentication.enabled" -> "true",
    "Test.basicAuthentication.realm" -> "Test",
    "Test.basicAuthentication.username" -> testUserName,
    "Test.basicAuthentication.password" -> testPassword
  )

  lazy val mockConf = app.injector.instanceOf[Configuration]
  lazy val mockControllerComponents = app.injector.instanceOf[ControllerComponents]
  val mockServicesConfig = mock[ServicesConfig]

  val bafc = new BasicAuthenticationFilterConfiguration("1234", false, "username", "password")
  val mockAuthAction = new BasicAuthenticatedAction(bafc, mockControllerComponents)
  val authAction = mockAuthAction

  class Setup {

    object TestController extends Ping(mockConf, mockControllerComponents, mockServicesConfig) {
      override val authAction = mockAuthAction
    }

  }

  "GET /ping/noauth" should {
    "return 200" in new Setup {
      val fakeRequest = FakeRequest("GET", "/ping/noauth")
      val result = TestController.noAuth()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "GET /ping" should {
    "return 401 if no creds" in {
      val controller = new Ping(mockConf, mockControllerComponents, mockServicesConfig) {

        val bafc2 = new BasicAuthenticationFilterConfiguration("1234", true, "username", "password")
        override val authAction = new BasicAuthenticatedAction(bafc2, mockControllerComponents)
      }
      val fakeRequest = FakeRequest("GET", "/ping")
      val result = controller.auth()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return 200 if the username and password match the config" in new Setup {
      val fakeRequest = FakeRequest("GET", "/ping").withHeaders("Authorization" -> "Basic Zm9vOmJhcg==")
      val result = TestController.auth()(fakeRequest)
      status(result) shouldBe Status.OK
    }

  }

}
