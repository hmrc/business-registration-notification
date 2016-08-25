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

package uk.gov.hmrc.businessregistrationnotification.controllers

import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import play.api.mvc._
import uk.gov.hmrc.businessregistrationnotification.WSHttp
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, HttpReads, HttpResponse}
import play.api.Logger

import scala.concurrent.Future

object Ping extends Ping with ServicesConfig {
	val rootUrl = baseUrl("business-registration-frontend")
	val http = WSHttp
}

trait Ping extends BaseController {

	val rootUrl: String
	val http: HttpGet

	def noAuth() = Action.async { implicit request =>
		Future.successful(Ok(""))
	}

	def proxy() = Action.async { implicit request =>

		def proxyToBR()(implicit rds: HttpReads[HttpResponse], hc: HeaderCarrier) = {

			val url = s"${rootUrl}/register-your-business/welcome"
			http.GET[HttpResponse](url)(rds, hc.withExtraHeaders("notification" -> "true"))
		}

		Logger.info("About to call proxy service")
		proxyToBR().map( x =>
			Logger.info(s"Called proxy service and got status ${x.status}")
		)

		Future.successful(Ok(""))
	}

}
