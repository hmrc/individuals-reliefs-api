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
import v1.models.errors.FormatNameErrorGenerator

class NameValidationSpec extends UnitSpec {

  val validName: Option[String] = Some("Company Inc")
  val invalidName: Option[String] = Some("AA1234*&^%$£BBCBCBC")

  "validate" should {
    "return no errors" when {
      "a valid name is supplied" in {
        val validationResult = NameValidation.validateOptional(validName, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe true
      }
      "no name is supplied" in {
        val validationResult = NameValidation.validateOptional(None, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {
      "an invalid name is supplied" in {
        val validationResult = NameValidation.validateOptional(invalidName, "/vctSubscription/1/name")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe FormatNameErrorGenerator.generate(Seq("/vctSubscription/1/name"))
      }
    }
  }
}