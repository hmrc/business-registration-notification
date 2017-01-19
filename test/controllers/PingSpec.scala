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

import play.api.http.Status
import play.api.test.{FakeApplication, FakeRequest}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class PingSpec extends UnitSpec with WithFakeApplication {

  val testUserName = "foo"
  val testPassword = "bar"

  override lazy val fakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.basicAuthentication.enabled" -> "true",
    "Test.basicAuthentication.realm" -> "Test",
    "Test.basicAuthentication.username" -> testUserName,
    "Test.basicAuthentication.password" -> testPassword
  ))

  "GET /ping/noauth" should {
    val fakeRequest = FakeRequest("GET", "/ping/noauth")
    "return 200" in {
      val result = Ping.noAuth()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "GET /ping" should {
    "return 401 if no creds" in {
      val fakeRequest = FakeRequest("GET", "/ping")
      val result = Ping.auth()(fakeRequest)
      status(result) shouldBe Status.UNAUTHORIZED
    }

    "return 200 if the username and password match the config" in {
      val fakeRequest = FakeRequest("GET", "/ping").withHeaders("Authorization"->"Basic Zm9vOmJhcg==")
      val result = Ping.auth()(fakeRequest)
      status(result) shouldBe Status.OK
    }

  }

}
