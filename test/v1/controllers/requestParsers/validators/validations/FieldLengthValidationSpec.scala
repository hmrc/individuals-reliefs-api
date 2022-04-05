/*
 * Copyright 2022 HM Revenue & Customs
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
import v1.models.errors.CustomerReferenceFormatError

class FieldLengthValidationSpec extends UnitSpec {

  val validReference: Option[String]           = Some("123")
  val invalidReferenceTooShort: Option[String] = Some("")
  val invalidReferenceTooLong: Option[String]  = Some("1234")

  "validate" should {
    "return no errors" when {
      "a valid reference is supplied" in {
        val validationResult = FieldLengthValidation.validateOptional(validReference, 3, "/field", CustomerReferenceFormatError)
        validationResult.isEmpty shouldBe true
      }
      "no reference is supplied" in {
        val validationResult = FieldLengthValidation.validateOptional(None, 3, "/field", CustomerReferenceFormatError)
        validationResult.isEmpty shouldBe true
      }
    }
    "return an error" when {
      "the supplied reference is too short" in {
        val validationResult = FieldLengthValidation.validateOptional(invalidReferenceTooShort, 3, "/field", CustomerReferenceFormatError)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe CustomerReferenceFormatError.copy(paths = Some(Seq("/field")))
      }
      "the supplied reference is too long" in {
        val validationResult = FieldLengthValidation.validateOptional(invalidReferenceTooLong, 3, "/field", CustomerReferenceFormatError)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe CustomerReferenceFormatError.copy(paths = Some(Seq("/field")))
      }
    }
  }

}
