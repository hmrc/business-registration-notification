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

package services

import connectors.CompanyRegistrationConnector
import models.CompanyRegistrationPost
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object CompanyRegistrationService extends CompanyRegistrationService {
  val crConnector = CompanyRegistrationConnector
}

trait CompanyRegistrationService {

  val crConnector : CompanyRegistrationConnector

  def sendToCompanyRegistration(ackRef : String, crPost : CompanyRegistrationPost)(implicit hc : HeaderCarrier) : Future[Int] = {
    crConnector.processAcknowledgment(ackRef, crPost) map {
      resp =>
        Logger.info(s"[CompanyRegistrationService] - [sendToCompanyRegistration] - status code : ${resp.status}, message : ${resp.body}")
        resp.status
    }
  }
}