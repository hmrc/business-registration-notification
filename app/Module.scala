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

import com.google.inject.AbstractModule
import config.{AppStartup, DefaultAppStartup}
import controllers.{NotificationController, NotificationCtrl, Ping, PingImp}
import services.{MetricsService, MetricsServiceImp, RegistrationService, RegistrationSrv}

/**
  * Created by jackie on 09/02/17.
  */
class Module extends AbstractModule {

  val gridFSNAme = "brn"

  override def configure(): Unit = {
    bind(classOf[AppStartup])
      .to(classOf[DefaultAppStartup])
      .asEagerSingleton()

    bind(classOf[NotificationCtrl]) to classOf[NotificationController]
    bind(classOf[Ping]) to classOf[PingImp]

    bindServices()

  }

  private def bindServices() {
    bind(classOf[MetricsService]) to classOf[MetricsServiceImp]
    bind(classOf[RegistrationSrv]) to classOf[RegistrationService]
  }

}
