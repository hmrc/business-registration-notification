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

package processors

import models.ETMPNotification
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait RegimeProcessor extends Logging {

  class AuditError(msg: String) extends Exception {
    override def getMessage: String = msg
  }

  def processRegime(ackRef: String, data: ETMPNotification)(implicit hc: HeaderCarrier): Future[Int]

  protected def handleProcessRegimeError(e: Throwable, downstreamCall: => Future[Int], identifier: String): Future[Int] = {
    e match {
      case auditErr: AuditError =>
        logger.error(s"$identifier: Audit event failed because ${auditErr.getMessage}")
        downstreamCall
      case err =>
        logger.error(s"$identifier: Unexpected error - update failed because: '${err.getMessage}'")
        throw err
    }
  }
}
