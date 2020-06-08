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
import v1.models.errors.CustomerReferenceFormatError

class CustomerReferenceValidationSpec extends UnitSpec {

  val validReference = Some("HJ812JJMNS89SJ09KLJNBH89O")
  val invalidReference = Some("ABCDEFGHIJKLMNOPQRSTUVWXYZ")

  "validate" should {
    "return no errors" when {
      "a valid reference is supplied" in {
        val validationResult = CustomerReferenceValidation.validateOptional(validReference, "/annualPaymentsMade/customerReference")
        validationResult.isEmpty shouldBe true
      }
      "no reference is supplied" in {
        val validationResult = CustomerReferenceValidation.validateOptional(None, "/annualPaymentsMade/customerReference")
        validationResult.isEmpty shouldBe true
      }
    }
    "return an error" when {
      "an invalid reference is supplied" in {
        val validationResult = CustomerReferenceValidation.validateOptional(invalidReference, "/annualPaymentsMade/customerReference")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe CustomerReferenceFormatError.copy(paths = Some(Seq("/annualPaymentsMade/customerReference")))
      }
    }
  }
}
