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

package connectors

import config.WSHttp
import mocks.MockHttp
import models.CompanyRegistrationPost
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.test.Helpers._
import test.UnitSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class CompanyRegistrationConnectorSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with MockHttp {

  val mockHttp = mock[WSHttp]

  val successResponse = mockHttpResponse(OK)

  implicit val hc = new HeaderCarrier()

  class Setup {
    object TestConnector extends RegistrationConnector {
      val companyRegUrl = "testUrl"
      val http = mockHttp
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
        (ArgumentMatchers.anyString(), ArgumentMatchers.any[JsValue](), ArgumentMatchers.any())
        (ArgumentMatchers.any[Writes[JsValue]](), ArgumentMatchers.any[HttpReads[HttpResponse]](), ArgumentMatchers.any[HeaderCarrier](), ArgumentMatchers.any[ExecutionContext]()))
        .thenReturn(Future.successful(successResponse))

      val result = await(TestConnector.processAcknowledgment("testID", crPost))
      result.status shouldBe OK
    }
  }
}
