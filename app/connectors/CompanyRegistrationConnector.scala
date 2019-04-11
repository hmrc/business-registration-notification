/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import config._
import models.CompanyRegistrationPost
import play.api.{Configuration, Play}
import play.api.Mode.Mode
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.Future
import uk.gov.hmrc.http.{CorePost, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

@Singleton
class CompanyRegistrationConnector @Inject() (val http: WSHttp) extends RegistrationConnector with ServicesConfig {
  lazy val companyRegUrl = s"${baseUrl("company-registration")}/company-registration"

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}

trait RegistrationConnector {

  val companyRegUrl : String
  val http : CorePost

  def processAcknowledgment(ackRef : String, crPost : CompanyRegistrationPost)(implicit hc : HeaderCarrier) : Future[HttpResponse] = {
    val json = Json.toJson(crPost)
    http.POST[JsValue, HttpResponse](s"$companyRegUrl/corporation-tax-registration/acknowledgement-confirmation?ackref=$ackRef", json)
  }
}
