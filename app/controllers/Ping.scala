/*
 * Copyright 2023 HM Revenue & Customs
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

import basicauth.{BasicAuthenticatedAction, BasicAuthentication}
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class Ping @Inject()(val config: Configuration,
                     cc: ControllerComponents,
                     val serviceConfig: ServicesConfig) extends BackendController(cc) with BasicAuthentication {
  val authAction: ActionBuilder[Request, AnyContent] = new BasicAuthenticatedAction(getBasicAuthConfig, cc)

  def noAuth(): Action[AnyContent] = Action {
    Ok
  }

  def auth(): Action[AnyContent] = authAction {
    Ok
  }
}
