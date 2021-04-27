/*
 * Copyright 2021 HM Revenue & Customs
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

import models.CompanyRegistrationPost
import play.api.libs.json.{JsValue, Json}
import play.api.{Configuration, Mode}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CompanyRegistrationConnector @Inject()(http: HttpClient,
                                             servicesConfig: ServicesConfig,
                                             configuration: Configuration) {

  lazy val companyRegUrl = s"${servicesConfig.baseUrl("company-registration")}/company-registration"

  protected def mode: Mode = Mode.Prod

  protected def runModeConfiguration: Configuration = configuration

  def processAcknowledgment(ackRef: String,
                            crPost: CompanyRegistrationPost
                           )(implicit hc: HeaderCarrier,
                             ec: ExecutionContext): Future[HttpResponse] = {
    val json = Json.toJson(crPost)
    http.POST[JsValue, HttpResponse](s"$companyRegUrl/corporation-tax-registration/acknowledgement-confirmation?ackref=$ackRef", json)
  }
}
