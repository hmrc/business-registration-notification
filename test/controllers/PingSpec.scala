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
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.Configuration
import play.api.Mode.Mode
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import test.UnitSpec

class PingSpec extends UnitSpec with OneAppPerSuite with MockitoSugar {

  val testUserName = "foo"
  val testPassword = "bar"

  override lazy val fakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.basicAuthentication.enabled" -> "true",
    "Test.basicAuthentication.realm" -> "Test",
    "Test.basicAuthentication.username" -> testUserName,
    "Test.basicAuthentication.password" -> testPassword
  ))

  val mockConf = app.injector.instanceOf[Configuration]

  val bafc = new BasicAuthenticationFilterConfiguration("1234", false, "username", "password")
  val mockAuthAction = new BasicAuthenticatedAction(bafc)
  val authAction = mockAuthAction

  class Setup {

    object TestController extends Ping(mockConf) {
      override val authAction = mockAuthAction

      override protected def mode: Mode = fakeApplication.mode

      override protected def runModeConfiguration: Configuration = fakeApplication.configuration
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
      val controller = new Ping(mockConf) {
        override protected def mode: Mode = fakeApplication.mode

        override protected def runModeConfiguration: Configuration = fakeApplication.configuration

        val bafc2 = new BasicAuthenticationFilterConfiguration("1234", true, "username", "password")
        override val authAction = new BasicAuthenticatedAction(bafc2)
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
