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

package models

import org.scalatest.Assertion
import org.scalatest.matchers.should
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.validation.ValidationError
import play.api.libs.json._

trait JsonFormatValidation {
  this: AnyWordSpec with should.Matchers =>

  def shouldBeSuccess[T](expected: T, result: JsResult[T]): Assertion = {
    result match {
      case JsSuccess(value, _) => value shouldBe expected
      case JsError(errors) => fail(s"Test produced errors - $errors")
    }
  }

  def shouldHaveErrors[T](result: JsResult[T], errorPath: JsPath, expectedError: JsonValidationError): Unit = {
    shouldHaveErrors[T](result, Map(errorPath -> Seq(expectedError)))
  }

  def shouldHaveErrors[T](result: JsResult[T], errorPath: JsPath, expectedErrors: Seq[JsonValidationError]): Unit = {
    shouldHaveErrors[T](result, Map(errorPath -> expectedErrors))
  }

  def shouldHaveErrors[T](result: JsResult[T], expectedErrors: Map[JsPath, Seq[JsonValidationError]]): Unit = {
    result match {
      case JsSuccess(value, _) => fail(s"read should have failed and didn't - produced $value")
      case JsError(errors) =>
        errors.length shouldBe expectedErrors.keySet.toSeq.length

        for (error <- errors) {
          error match {
            case (path, valErrs) =>
              expectedErrors.keySet should contain(path)
              expectedErrors(path) shouldBe valErrs
          }
        }
    }
  }

  def shouldHaveErrors2[T](result: JsResult[T], errorPath: JsPath, expectedError: ValidationError): Assertion = {
    result match {
      case JsSuccess(value, _) => fail(s"read should have failed and didn't - produced $value")
      case JsError(errors) =>
        errors.length shouldBe 1
        errors.head match {
          case (path, error) =>
            path shouldBe errorPath
            error.length shouldBe 1
            error shouldBe expectedError
        }
    }
  }
}
