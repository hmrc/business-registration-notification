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

package models

import play.api.libs.json.{Format, Reads, Writes}
import Reads.{maxLength, minLength, pattern}
import play.api.libs.functional.syntax._


object Validation {

  def length(maxLen:Int, minLen: Int = 1): Reads[String] = maxLength[String](maxLen) keepAnd minLength[String](minLen)
  def readToFmt(rds:Reads[String])(implicit wts:Writes[String]): Format[String] = Format(rds,wts)
  def lengthFmt(maxLen:Int, minLen: Int = 1): Format[String] = readToFmt(length(maxLen, minLen))

}

trait NotificationValidator {
  import Validation.lengthFmt

  val taxIdentifierValidator = lengthFmt(15)
  val statusValidator = pattern("^04|05|06|07|08|09|10$".r)
  val regimeValidator = pattern("^corporation-tax$".r)
  val isoDateValidator = pattern("^\\d{4}(-\\d\\d(-\\d\\d(T\\d\\d:\\d\\d:\\d\\d(\\.\\d+)?(([+-]\\d\\d:\\d\\d)|Z)?)?)?)?$".r)
}
