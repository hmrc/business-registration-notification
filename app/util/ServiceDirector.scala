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

package util

import config.Regimes
import models.ETMPNotification
import play.api.Logger
import processors.{CTProcessor, PAYEProcessor}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class ServiceDirector @Inject()(val payeRegimeProcessor: PAYEProcessor,
                                val ctRegimeProcessor: CTProcessor) extends Regimes {

  def goToService(ackRef: String, regime: String, data: ETMPNotification)(implicit hc: HeaderCarrier): Future[Int] = {
    regime match {
      case CORPORATION_TAX => ctRegimeProcessor.processRegime(ackRef, data)
      case PAYE => payeRegimeProcessor.processRegime(ackRef, data)
      case _ =>
        Logger.info(s"[ServiceDirector] - [goToService] : An unsupported tax regime was presented")
        Future.successful(INVALID_REGIME)
    }
  }
}
