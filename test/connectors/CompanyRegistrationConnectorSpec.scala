/*
 * Copyright 2022 HM Revenue & Customs
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

import mocks.MockHttp
import models.CompanyRegistrationPost
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CompanyRegistrationConnectorSpec extends AnyWordSpec with should.Matchers with MockitoSugar with MockHttp {

  val mockHttp: HttpClient = mock[HttpClient]
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockConfiguration: Configuration = mock[Configuration]

  val successResponse: HttpResponse = mockHttpResponse(OK)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class Setup {

    object TestConnector extends CompanyRegistrationConnector(mockHttp, mockServicesConfig, mockConfiguration) {
      override lazy val companyRegUrl = "testUrl"
    }

  }

  "processAcknowledgment" should {
    val crPost = CompanyRegistrationPost(
      Some("testID"),
      "testTimeStamp",
      "testStatus"
    )
    Json.toJson(crPost)

    "return a HTTPResponse" in new Setup {
      when(mockHttp.POST[JsValue, HttpResponse]
        (ArgumentMatchers.anyString(), ArgumentMatchers.any[JsValue](), ArgumentMatchers.any())
        (ArgumentMatchers.any[Writes[JsValue]](),
          ArgumentMatchers.any[HttpReads[HttpResponse]](),
          ArgumentMatchers.any[HeaderCarrier](),
          ArgumentMatchers.any[ExecutionContext]()))
        .thenReturn(Future.successful(successResponse))

      val result: HttpResponse = await(TestConnector.processAcknowledgment("testID", crPost))
      result.status shouldBe OK
    }
  }
}
