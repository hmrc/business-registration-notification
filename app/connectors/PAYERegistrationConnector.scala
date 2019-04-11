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
import config.WSHttp
import models.PAYERegistrationPost
import play.api.{Configuration, Play}
import play.api.Mode.Mode
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.Future
import uk.gov.hmrc.http.{CorePost, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

@Singleton
class PAYERegistrationConnector @Inject() (val http: WSHttp)

  extends PAYERegistrationConnect with ServicesConfig {
  lazy val payeRegUrl = s"${baseUrl("paye-registration")}/paye-registration"

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}

trait PAYERegistrationConnect {
  val payeRegUrl : String
  val http : CorePost

  def processAcknowledgement(ackRef: String, payePost: PAYERegistrationPost)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val json = Json.toJson(payePost)
    http.POST[JsValue, HttpResponse](s"$payeRegUrl/registration-processed-confirmation?ackref=$ackRef", json)
  }
}
