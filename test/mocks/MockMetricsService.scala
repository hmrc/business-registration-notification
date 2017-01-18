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

package mocks

import com.codahale.metrics.{Counter, Timer}
import org.scalatest.mock.MockitoSugar
import services.MetricsService

object MockMetricsService extends MetricsService with MockitoSugar {
  val fakeCounter = mock[Counter]

  override val etmpNotificationCounter: Counter = fakeCounter
  override val ackRefNotFound: com.codahale.metrics.Counter = fakeCounter
  override val clientErrorCodes: com.codahale.metrics.Counter = fakeCounter
  override val internalServerError: com.codahale.metrics.Counter = fakeCounter
  override val serverErrorCodes: com.codahale.metrics.Counter = fakeCounter
  override val serviceNotAvailable: com.codahale.metrics.Counter = fakeCounter

}
