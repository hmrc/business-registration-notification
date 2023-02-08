/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec

class BasicAuthCredentialsTest extends AnyWordSpec with should.Matchers {

  "conversion of a known value succeeds correctly" in {
    val creds = BasicAuthCredentials.fromAuthorizationHeader(Some("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="))
    assert(creds === Some(BasicAuthCredentials("Aladdin", "open sesame")))
  }

  "round-trip conversion succeeds correctly" in {
    val creds1 = BasicAuthCredentials("Aladdin", "open sesame")
    assert(BasicAuthCredentials.fromBase64(creds1.toBase64) === Some(creds1))
    val creds2 = BasicAuthCredentials("", "")
    assert(BasicAuthCredentials.fromBase64(creds2.toBase64) === Some(creds2))
  }
}
