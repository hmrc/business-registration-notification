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

package controllers

import javax.inject.Inject
import basicauth.{BasicAuthenticatedAction, BasicAuthentication}
import com.google.inject.Singleton
import config.WSHttp
import play.api.Configuration
import play.api.mvc._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.microservice.controller.BaseController
import scala.concurrent.Future

@Singleton
class PingImp @Inject() (override val config: Configuration) extends Ping {
  val authAction: ActionBuilder[Request] = {
    new BasicAuthenticatedAction(getBasicAuthConfig())
  }

}

trait Ping extends BaseController with ServicesConfig with BasicAuthentication {
  val authAction: ActionBuilder[Request]

  def noAuth() = Action {
    Ok
  }

  def auth() = authAction {
    Ok
  }

}
