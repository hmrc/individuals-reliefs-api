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
import v1.models.errors.InvestmentRefFormatError
import v1.models.utils.JsonErrorValidators

class InvestmentRefValidationSpec extends UnitSpec with JsonErrorValidators {

  val validRef: Option[String] = Some("123412/1A")
  val invalidRef: Option[String] = Some("AA1234*&^%$£BBCBCBC")

  "validate" should {
    "return no errors" when {
      "a valid unique investment reference is supplied" in {
        val validationResult = InvestmentRefValidation.validateOptional(validRef,"/vctSubscription/0/uniqueInvestmentRef")
        validationResult.isEmpty shouldBe true
      }
      "no unique investment reference is supplied" in {
        val validationResult = InvestmentRefValidation.validateOptional(None,"/vctSubscription/0/uniqueInvestmentRef")
        validationResult.isEmpty shouldBe true
      }
    }

    "return an error" when {
      "when an invalid name is supplied" in {
        val validationResult = InvestmentRefValidation.validateOptional(invalidRef, "/vctSubscription/0/uniqueInvestmentRef")
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe  InvestmentRefFormatError.copy(paths = Some(Seq("/vctSubscription/0/uniqueInvestmentRef")))
      }
    }
  }

}