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

package v1.controllers.requestParsers.validators

import mocks.MockAppConfig
import support.UnitSpec
import v1.models.errors.{NinoFormatError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import v1.models.request.retrievePensionsReliefs.RetrievePensionsReliefsRawData

class RetrievePensionsReliefsValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2020-21"

  class Test {
    val validator = new RetrievePensionsReliefsValidator(mockAppConfig)
    MockedAppConfig.pensionsReliefsMinimumTaxYear returns 2022 anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(RetrievePensionsReliefsRawData(validNino, validTaxYear))
      }
    }
    "return NinoFormatError" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(RetrievePensionsReliefsRawData("A12344A", validTaxYear))
      }
    }
    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(RetrievePensionsReliefsRawData(validNino, "201831")) shouldBe List(TaxYearFormatError)
      }
    }
    "return RuleTaxYearRangeInvalidError" when {
      "the tax year range exceeds 1" in new Test {
        validator.validate(RetrievePensionsReliefsRawData(validNino, "2019-21")) shouldBe List(RuleTaxYearRangeInvalidError)
      }
    }
    "return RuleTaxYearNotSupportedError" when {
      "the given tax year is before the minimum tax year" in new Test {
        validator.validate(RetrievePensionsReliefsRawData(validNino, "2019-20")) shouldBe List(RuleTaxYearNotSupportedError)
      }
    }
    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(RetrievePensionsReliefsRawData("A12344A", "20178")) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
    }
  }
}
