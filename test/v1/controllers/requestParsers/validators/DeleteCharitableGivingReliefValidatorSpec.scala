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

package v1.controllers.requestParsers.validators

import api.models.errors.{NinoFormatError, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import support.UnitSpec
import v1.models.request.deleteCharitableGivingTaxRelief.DeleteCharitableGivingTaxReliefRawData

class DeleteCharitableGivingReliefValidatorSpec extends UnitSpec {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2017-18"

  val validator = new DeleteCharitableGivingReliefValidator

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(DeleteCharitableGivingTaxReliefRawData(validNino, validTaxYear)) shouldBe Nil
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        validator.validate(DeleteCharitableGivingTaxReliefRawData("BADNINO", validTaxYear)) shouldBe List(NinoFormatError)
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        validator.validate(DeleteCharitableGivingTaxReliefRawData(validNino, "BADTAXYEAR")) shouldBe List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "an invalid tax year is supplied" in {
        validator.validate(DeleteCharitableGivingTaxReliefRawData(validNino, "2015-17")) shouldBe List(RuleTaxYearRangeInvalidError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a tax year before the earliest allowed date is supplied" in {
        validator.validate(DeleteCharitableGivingTaxReliefRawData(validNino, "2016-17")) shouldBe List(RuleTaxYearNotSupportedError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(DeleteCharitableGivingTaxReliefRawData("BADNINO", "BADTAXYEAR")) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
