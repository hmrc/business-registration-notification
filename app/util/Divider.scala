/*
 * Copyright 2021 HM Revenue & Customs
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

package util

object Divider {

  implicit class IDivider(s: String) {
    def divide(c: Char): List[String] = Divider.divide(s, c)

    def divideLast(c: Char): List[String] = Divider.divideLast(s, c)
  }

  /** Divides at the first instance of c */
  def divide(s: String, c: Char): List[String] = divide(s, s.indexOf(c))

  /** Divides at the last instance of c */
  def divideLast(s: String, c: Char): List[String] = divide(s, s.lastIndexOf(c))

  private def divide(s: String, i: Int) = {
    i match {
      case i if i >= 0 => List(s.substring(0, i), s.substring(i + 1))
      case _ => List(s)
    }

  }
}
