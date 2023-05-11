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

package api.controllers.requestParsers.validators.validations

import api.models.errors.CustomerReferenceFormatError
import support.UnitSpec

class ReferenceRegexValidationSpec extends UnitSpec {

  val validReference: Option[String]   = Some("HJ812JJMNS89SJ09KLJNBH89O")
  val invalidReference: Option[String] = Some(("1234567890" * 9) + "1")

  "validate" should {
    "return no errors" when {
      "a valid reference is supplied" in {
        val validationResult = ReferenceRegexValidation.validateOptional(
          validReference,
          "/annualPaymentsMade/customerReference",
          CustomerReferenceFormatError
        )
        validationResult.isEmpty shouldBe true
      }
      "no reference is supplied" in {
        val validationResult = ReferenceRegexValidation.validateOptional(
          None,
          "/annualPaymentsMade/customerReference",
          CustomerReferenceFormatError
        )
        validationResult.isEmpty shouldBe true
      }
    }
    "return an error" when {
      "an invalid reference is supplied" in {
        val validationResult = ReferenceRegexValidation.validateOptional(
          invalidReference,
          "/annualPaymentsMade/customerReference",
          CustomerReferenceFormatError
        )
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe CustomerReferenceFormatError.copy(paths = Some(Seq("/annualPaymentsMade/customerReference")))
      }
    }
  }

}
