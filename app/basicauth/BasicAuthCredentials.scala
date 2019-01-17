/*
 * Copyright 2019 HM Revenue & Customs
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

package basicauth

import util.{BasicBase64, Divider}

case class BasicAuthCredentials(username: String, password: String) {
  val conjoined = username + ':' + password

  def toBase64: String = BasicBase64.encodeToString(conjoined)
}

object BasicAuthCredentials {
  import Divider.IDivider
  def fromAuthorizationHeader(os: Option[String]): Option[BasicAuthCredentials] = {
    os flatMap { s =>
      s.divide(' ') match {
        case "Basic" :: b64 :: Nil => fromBase64(b64)
        case _ => None
      }
    }
  }

  def fromBase64(b64: String): Option[BasicAuthCredentials] = {
    BasicBase64.decodeToString(b64).divide(':') match {
      case w1 :: w2 :: Nil => Some(new BasicAuthCredentials(w1, w2))
      case _ => None
    }
  }
}
