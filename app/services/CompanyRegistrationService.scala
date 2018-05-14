/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import connectors.{CompanyRegistrationConnector, PAYERegistrationConnector}
import models.{CompanyRegistrationPost, PAYERegistrationPost}

import scala.concurrent.Future
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class RegistrationService @Inject() (companyRegistrationConnector: CompanyRegistrationConnector,
                                     payeRegistrationConnector: PAYERegistrationConnector) extends RegistrationSrv {

  val crConnector = companyRegistrationConnector
  val payeConnector = payeRegistrationConnector
}

trait RegistrationSrv {

  val crConnector: CompanyRegistrationConnector
  val payeConnector: PAYERegistrationConnector

  def sendToCompanyRegistration(ackRef: String, crPost: CompanyRegistrationPost)(implicit hc: HeaderCarrier): Future[Int] = {
    crConnector.processAcknowledgment(ackRef, crPost) map (_.status)
  }

  def sendToPAYERegistration(ackRef: String, payePost: PAYERegistrationPost)(implicit hc: HeaderCarrier): Future[Int] = {
    payeConnector.processAcknowledgement(ackRef, payePost) map (_.status)
  }
}
