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

package v1.controllers.validators

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import support.UnitSpec
import v1.models.request.retrievePensionsReliefs.RetrievePensionsReliefsRequestData

class RetrievePensionsReliefsValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"

  private val parsedNino = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  class Test {
    val validatorFactory = new RetrievePensionsReliefsValidatorFactory
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        val result: Either[ErrorWrapper, RetrievePensionsReliefsRequestData] = validatorFactory.validator(validNino, validTaxYear).validateAndWrapResult()
        result shouldBe Right(RetrievePensionsReliefsRequestData(parsedNino, parsedTaxYear))
      }
    }
    "return NinoFormatError" when {
      "an invalid nino is supplied" in new Test {
        val result: Either[ErrorWrapper, RetrievePensionsReliefsRequestData] = validatorFactory.validator("A12344A", validTaxYear).validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in new Test {
        val result: Either[ErrorWrapper, RetrievePensionsReliefsRequestData] = validatorFactory.validator(validNino, "201831").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }
    "return RuleTaxYearRangeInvalidError" when {
      "the tax year range exceeds 1" in new Test {
        val result: Either[ErrorWrapper, RetrievePensionsReliefsRequestData] = validatorFactory.validator(validNino, "2019-21").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }
    "return RULE_TAX_YEAR_NOT_SUPPORTED error" when {
      "a tax year before the earliest allowed date is supplied" in new Test {
        val result: Either[ErrorWrapper, RetrievePensionsReliefsRequestData] = validatorFactory.validator(validNino, "2016-17").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }
    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        val result: Either[ErrorWrapper, RetrievePensionsReliefsRequestData] = validatorFactory.validator("A12344A", "20178").validateAndWrapResult()
        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}