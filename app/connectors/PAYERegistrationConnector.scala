/*
 * Copyright 2017 HM Revenue & Customs
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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.Future
import uk.gov.hmrc.http.{CorePost, HeaderCarrier, HttpResponse}

import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

@Singleton
class PAYERegistrationConnector extends PAYERegistrationConnect with ServicesConfig {
  lazy val payeRegUrl = s"${baseUrl("paye-registration")}/paye-registration"
  val http: CorePost = WSHttp
}

trait PAYERegistrationConnect {
  val payeRegUrl : String
  val http : CorePost

  def processAcknowledgement(ackRef: String, payePost: PAYERegistrationPost)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val json = Json.toJson(payePost)
    http.POST[JsValue, HttpResponse](s"$payeRegUrl/registration-processed-confirmation?ackref=$ackRef", json)
  }
}
