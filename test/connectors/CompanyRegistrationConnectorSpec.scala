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

package connectors

import config.WSHttp
import mocks.MockHttp
import models.CompanyRegistrationPost
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.Matchers
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpReads, HttpResponse}
import play.api.test.Helpers._

import scala.concurrent.Future

class CompanyRegistrationConnectorSpec extends UnitSpec with WithFakeApplication with MockitoSugar with MockHttp {

  val mockHttp = mock[WSHttp]

  val successResponse = mockHttpResponse(OK)

  implicit val hc = new HeaderCarrier()

  class Setup {
    object TestConnector extends CompanyRegistrationConnector {
      val companyRegUrl = "testUrl"
      val http = mockHttp
    }
  }

  "CompanyRegistrationConnector" should {
    "use the correct companyRegUrl" in {
      CompanyRegistrationConnector.companyRegUrl shouldBe "http://localhost:9973/company-registration"
    }
    "use the correct http library" in {
      CompanyRegistrationConnector.http shouldBe WSHttp
    }
  }

  "processAcknowledgment" should {

    val crPost = CompanyRegistrationPost(
      Some("testID"),
      "testTimeStamp",
      "testStatus"
    )

    val crJson = Json.toJson(crPost)

    "return a HTTPResponse" in new Setup {
      when(mockHttp.POST[JsValue, HttpResponse]
        (Matchers.anyString(), Matchers.any[JsValue](), Matchers.any())
        (Matchers.any[Writes[JsValue]](), Matchers.any[HttpReads[HttpResponse]](), Matchers.any[HeaderCarrier]()))
        .thenReturn(Future.successful(successResponse))

      val result = await(TestConnector.processAcknowledgment("testID", crPost))
      result.status shouldBe OK
    }
  }
}
