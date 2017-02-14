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

import basicauth.{BasicAuthenticatedAction, BasicAuthenticationFilterConfiguration}
import config.WSHttp
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import play.api.Configuration
import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeApplication, FakeRequest}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future


class PingSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  val testUserName = "foo"
  val testPassword = "bar"

  override lazy val fakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.basicAuthentication.enabled" -> "true",
    "Test.basicAuthentication.realm" -> "Test",
    "Test.basicAuthentication.username" -> testUserName,
    "Test.basicAuthentication.password" -> testPassword
  ))

  val mockConf = mock[Configuration]
  val mockWSHttp = mock[WSHttp]

  class Setup {
    object TestController extends Ping {
      override val authAction = mockAuthAction
      val config = mockConf
      val http = mockWSHttp

    }
  }

  val bafc = new BasicAuthenticationFilterConfiguration("1234", false, "username", "password")
  val mockAuthAction = new BasicAuthenticatedAction(bafc)
  val authAction = mockAuthAction

  "GET /ping/noauth" should {
    "return 200" in new Setup {
      val fakeRequest = FakeRequest("GET", "/ping/noauth")
      val result = TestController.noAuth()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "GET /ping" should {
    "return 401 if no creds" in {
      val controller = new Ping {
        override val config: Configuration = mockConf
        override val http = mockWSHttp
        val bafc2 = new BasicAuthenticationFilterConfiguration("1234", true, "username", "password")
        override val authAction = new BasicAuthenticatedAction(bafc2)
      }
      val fakeRequest = FakeRequest("GET", "/ping")
      val result = controller.auth()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return 200 if the username and password match the config" in new Setup {
      val fakeRequest = FakeRequest("GET", "/ping").withHeaders("Authorization"->"Basic Zm9vOmJhcg==")
      val result = TestController.auth()(fakeRequest)
      status(result) shouldBe Status.OK
    }

  }

}
