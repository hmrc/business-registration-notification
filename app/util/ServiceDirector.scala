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

package util

import config.Regimes
import models.ETMPNotification
import models.ETMPNotification.convertToCRPost
import play.api.Logger
import services.CompanyRegistrationService
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object ServiceDirector extends ServiceDirector {
  val ctService = CompanyRegistrationService
}

trait ServiceDirector extends Regimes {

  val ctService : CompanyRegistrationService

  def goToService(ackRef : String, regime : String, data : ETMPNotification)(implicit hc : HeaderCarrier) : Future[Int] = {
    regime match {
      case CORPORATION_TAX => ctService.sendToCompanyRegistration(ackRef, convertToCRPost(data))
      case _ =>
        Logger.info(s"[ServiceDirector] - [goToService] : An unsupported tax regime was presented")
        Future.successful(INVALID_REGIME)
    }
  }
}
