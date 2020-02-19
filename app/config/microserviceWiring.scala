/*
 * Copyright 2020 HM Revenue & Customs
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

package config


import akka.actor.ActorSystem
import com.typesafe.config.Config
import config.filters.MicroserviceAuditConnector
import javax.inject.Inject
import play.api.Mode.Mode
import play.api.{Configuration, Play}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode}
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig

class MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"auditing")

  override protected def mode: Mode = Play.current.mode

  override protected def runModeConfiguration: Configuration = Play.current.configuration
}

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override lazy val auditConnector = MicroserviceAuditConnector
}

trait WSHttp extends
  HttpGet with WSGet with
  HttpPut with WSPut with
  HttpPost with WSPost with
  HttpDelete with WSDelete with
  HttpPatch with WSPatch with
  Hooks with AppName

class WSHttpImpl @Inject()(val runModeConfiguration: Configuration,val actorSystem: ActorSystem) extends WSHttp {
  override protected def configuration: Option[Config] = Option(runModeConfiguration.underlying)

  override protected def appNameConfiguration: Configuration = runModeConfiguration
}

