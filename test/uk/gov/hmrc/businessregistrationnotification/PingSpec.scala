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

package uk.gov.hmrc.businessregistrationnotification

import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.businessregistrationnotification.controllers.Ping
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class PingSpec extends UnitSpec with WithFakeApplication{

  "GET /ping/noauth" should {
    val fakeRequest = FakeRequest("GET", "/ping/noauth")
    "return 200" in {
      val result = Ping.noAuth()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }

  "GET /ping/proxy" should {
    val fakeRequest = FakeRequest("GET", "/ping/proxy")
    "return 200" in {
      val result = Ping.proxy()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }
}
