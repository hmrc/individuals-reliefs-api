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

import api.controllers.requestParsers.validators.validations.validations.minimumTaxYear
import api.models.errors.RuleTaxYearNotSupportedError
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import support.UnitSpec

class MtdTaxYearValidationSpec extends UnitSpec with JsonErrorValidators with MockAppConfig {

  "validate" should {

    "return no errors" when {
      "a tax year greater than the minimum allowed tax year is supplied" in {
        val taxYear          = "2021-22"
        val validationResult = MtdTaxYearValidation.validate(taxYear, minimumTaxYear)
        validationResult.isEmpty shouldBe true

      }

      "the minimum allowed tax year is supplied" in {
        val taxYear          = "2020-21"
        val validationResult = MtdTaxYearValidation.validate(taxYear, minimumTaxYear)
        validationResult.isEmpty shouldBe true
      }

    }

    "return the given error" when {
      "a tax year below the minimum allowed tax year is supplied" in {
        val taxYear          = "2018-19"
        val validationResult = MtdTaxYearValidation.validate(taxYear, minimumTaxYear)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe RuleTaxYearNotSupportedError
      }
    }
  }

}
