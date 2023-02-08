/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.httpParsers.BaseHttpReads
import javax.inject.{Inject, Singleton}
import models.CompanyRegistrationPost
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CompanyRegistrationConnector @Inject()(val http: HttpClient, servicesConfig: ServicesConfig)(implicit val ec: ExecutionContext)
  extends BaseHttpReads {

  lazy val companyRegUrl = s"${servicesConfig.baseUrl("company-registration")}/company-registration"

  def processAcknowledgment(ackRef: String, crPost: CompanyRegistrationPost)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    try {
    http.POST[CompanyRegistrationPost, HttpResponse](s"$companyRegUrl/corporation-tax-registration/acknowledgement-confirmation?ackref=$ackRef", crPost)(CompanyRegistrationPost.format, rawReads, hc, ec)
  } catch {
      case ex: Exception =>
        logger.error(s"[processAcknowledgement] - Error posting processAcknowledgement - ${ex.getMessage}", ex)
        Future.failed(ex)
    }
}
