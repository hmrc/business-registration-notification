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

package services

import com.codahale.metrics.Counter
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}

@Singleton
class MetricsService @Inject()(val metricsInstance: Metrics) {

  val etmpNotificationCounter: Counter = metricsInstance.defaultRegistry.counter("etmp-notification-counter")

  val ackRefNotFound: Counter = metricsInstance.defaultRegistry.counter("ack-ref-not-found")
  val serviceNotAvailable: Counter = metricsInstance.defaultRegistry.counter("service-not-available")
  val internalServerError: Counter = metricsInstance.defaultRegistry.counter("internal-server-error")

  val clientErrorCodes: Counter = metricsInstance.defaultRegistry.counter("client-error-codes")
  val serverErrorCodes: Counter = metricsInstance.defaultRegistry.counter("server-error-codes")

}
