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

package mocks

import com.codahale.metrics.Counter
import org.scalatest.mockito.MockitoSugar

object MockMetricsService extends MockitoSugar {
  val fakeCounter = mock[Counter]

  val etmpNotificationCounter: Counter = fakeCounter
  val ackRefNotFound: com.codahale.metrics.Counter = fakeCounter
  val clientErrorCodes: com.codahale.metrics.Counter = fakeCounter
  val internalServerError: com.codahale.metrics.Counter = fakeCounter
  val serverErrorCodes: com.codahale.metrics.Counter = fakeCounter
  val serviceNotAvailable: com.codahale.metrics.Counter = fakeCounter

}
