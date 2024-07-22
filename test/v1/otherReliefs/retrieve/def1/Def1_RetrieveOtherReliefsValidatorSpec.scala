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

package v1.otherReliefs.retrieve.def1

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v1.otherReliefs.retrieve.RetrieveOtherReliefsValidatorFactory
import v1.otherReliefs.retrieve.def1.model.request.Def1_RetrieveOtherReliefsRequestData
import v1.otherReliefs.retrieve.model.request.RetrieveOtherReliefsRequestData

class Def1_RetrieveOtherReliefsValidatorSpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new RetrieveOtherReliefsValidatorFactory

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveOtherReliefsRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveOtherReliefsRequestData(parsedNino, parsedTaxYear))
      }
    }

    "should return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveOtherReliefsRequestData] =
          validator("invalidNino", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an incorrectly formatted taxYear is supplied" in {
        val result: Either[ErrorWrapper, RetrieveOtherReliefsRequestData] =
          validator(validNino, "202122").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, RetrieveOtherReliefsRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "an invalid tax year, before the minimum, is supplied" in {
        val result: Either[ErrorWrapper, RetrieveOtherReliefsRequestData] =
          validator(validNino, "2019-20").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, RetrieveOtherReliefsRequestData] =
          validator("invalidNino", "invalidTaxYear").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
