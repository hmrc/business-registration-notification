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

package services

import connectors.{CompanyRegistrationConnector, PAYERegistrationConnector}
import javax.inject.{Inject, Singleton}
import models.{CompanyRegistrationPost, PAYERegistrationPost}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class CompanyRegistrationService @Inject()(companyRegistrationConnector: CompanyRegistrationConnector,
                                           payeRegistrationConnector: PAYERegistrationConnector) {


  def sendToCompanyRegistration(ackRef: String, crPost: CompanyRegistrationPost)(implicit hc: HeaderCarrier): Future[Int] = {
    companyRegistrationConnector.processAcknowledgment(ackRef, crPost) map (_.status)
  }

  def sendToPAYERegistration(ackRef: String, payePost: PAYERegistrationPost)(implicit hc: HeaderCarrier): Future[Int] = {
    payeRegistrationConnector.processAcknowledgement(ackRef, payePost) map (_.status)
  }
}
