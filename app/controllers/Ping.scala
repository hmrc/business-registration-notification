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

package controllers

import javax.inject.Inject

import basicauth.{BasicAuthenticatedAction, BasicAuthenticationFilterConfiguration}
import com.google.inject.Singleton
import config.WSHttp
import play.api.mvc._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

@Singleton
class Ping @Inject() (basicAuthFilterConfig: BasicAuthenticationFilterConfiguration) extends BaseController with ServicesConfig {
  val http = WSHttp
  val authAction: ActionBuilder[Request] = {
    new BasicAuthenticatedAction(basicAuthFilterConfig)
  }

  def noAuth() = Action.async { implicit request =>
    Future.successful(Ok(""))
  }

  def auth() = authAction {
    Ok("")
  }
}
