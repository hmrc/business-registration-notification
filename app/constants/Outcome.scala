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

package constants

import models.ETMPNotification

object Outcome {

  val APPROVED                      = "04"
  val APPROVED_WITH_CONDITIONS      = "05"
  val REJECTED                      = "06"
  val REJECTED_UNDER_REVIEW_APPREAL = "07"
  val REVOKED                       = "08"
  val REVOKED_UNDER_REVIEW_APPREAL  = "09"
  val DEREGISTERED                  = "10"

  def successfulOutcome(data: ETMPNotification): Boolean = {
    data.status == APPROVED || data.status == APPROVED_WITH_CONDITIONS
  }

}
