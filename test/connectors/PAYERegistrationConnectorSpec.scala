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

package connectors

import config.WSHttp
import mocks.MockHttp
import models.{CompanyRegistrationPost, PAYERegistrationPost}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.test.Helpers.OK
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class PAYERegistrationConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with MockHttp {
  val mockHttp = mock[WSHttp]

  val successResponse = mockHttpResponse(OK)

  implicit val hc = new HeaderCarrier()

  class Setup {
    object TestConnector extends PAYERegistrationConnect {
      val payeRegUrl = "testUrl"
      val http = mockHttp
    }
  }


  "processAcknowledgment" should {

    val payePost = PAYERegistrationPost(
      Some("testID"),
      "testTimeStamp",
      "testStatus"
    )

    val payeJson = Json.toJson(payePost)

    "return a HTTPResponse" in new Setup {
      when(mockHttp.POST[JsValue, HttpResponse]
        (Matchers.anyString(), Matchers.any[JsValue](), Matchers.any())
        (Matchers.any[Writes[JsValue]](), Matchers.any[HttpReads[HttpResponse]](), Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(successResponse))

      val result = await(TestConnector.processAcknowledgement("testAckRef", payePost))
      result.status shouldBe OK
    }
  }
}
