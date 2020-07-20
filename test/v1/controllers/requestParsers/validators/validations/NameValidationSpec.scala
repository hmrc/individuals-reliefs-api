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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.NameFormatError

class NameValidationSpec extends UnitSpec {

  val validNameMinLength: Option[String] = Some("a")
  val validNameMaxLength: Option[String] = Some("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyza")
  val invalidNameTooShort: Option[String] = Some("")
  val invalidNameTooLong: Option[String] = Some("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzab")

  "validate" should {
    "return no errors" when {
      "a valid name is supplied with the minimum allowed length" in {
        val validationResult = NameValidation.validateOptional(validNameMinLength, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe true
      }
      "a valid name is supplied with the maximum allowed length" in {
        val validationResult = NameValidation.validateOptional(validNameMaxLength, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe true
      }
      "no name is supplied" in {
        val validationResult = NameValidation.validateOptional(None, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {
      "an invalid name is supplied less than the minimum allowed length" in {
        val validationResult = NameValidation.validateOptional(invalidNameTooShort, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe NameFormatError.copy(paths = Some(Seq("/vctSubscription/1/name")))
      }
      "an invalid name is supplied greater than the maximum allowed length" in {
        val validationResult = NameValidation.validateOptional(invalidNameTooLong, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe NameFormatError.copy(paths = Some(Seq("/vctSubscription/1/name")))
      }
    }
  }
}