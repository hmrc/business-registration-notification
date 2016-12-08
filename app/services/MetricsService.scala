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

package services

import javax.inject.Inject

import com.codahale.metrics.Counter
import com.kenshoo.play.metrics.Metrics
import play.api.Play

object MetricsService extends MetricsService {
  val metrics = Play.current.injector.instanceOf[Metrics]

  val etmpNotificationCounter : Counter = metrics.defaultRegistry.counter("etmp-notification-counter")

  val ackRefNotFound : Counter = metrics.defaultRegistry.counter("ack-ref-not-found")
  val serviceNotAvailable : Counter = metrics.defaultRegistry.counter("service-not-available")
  val internalServerError : Counter = metrics.defaultRegistry.counter("internal-server-error")

  val clientErrorCodes : Counter = metrics.defaultRegistry.counter("client-error-codes")
  val serverErrorCodes : Counter = metrics.defaultRegistry.counter("server-error-codes")
}

trait MetricsService {
  val etmpNotificationCounter : Counter

  val ackRefNotFound : Counter
  val serviceNotAvailable : Counter
  val internalServerError : Counter

  val clientErrorCodes : Counter
  val serverErrorCodes : Counter
}
