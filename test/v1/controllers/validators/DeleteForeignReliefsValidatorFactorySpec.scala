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
import v1.models.request.deleteForeignReliefs.DeleteForeignReliefsRequestData

class DeleteForeignReliefsValidatorFactorySpec extends UnitSpec {

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val validNino              = "AA123456A"
  private val validTaxYear           = "2021-22"

  private val parsedNino    = Nino("AA123456A")
  private val parsedTaxYear = TaxYear.fromMtd("2021-22")

  private val validatorFactory = new DeleteForeignReliefsValidatorFactory()

  private def validator(nino: String, taxYear: String) = validatorFactory.validator(nino, taxYear)

  "validator" should {
    "return no errors" when {
      "a valid request is supplied" in {
        val result: Either[ErrorWrapper, DeleteForeignReliefsRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(DeleteForeignReliefsRequestData(parsedNino, parsedTaxYear))

      }
    }
    "return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, DeleteForeignReliefsRequestData] =
          validator("A12344A", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "an invalid tax year format is supplied" in {
        val result: Either[ErrorWrapper, DeleteForeignReliefsRequestData] =
          validator(validNino, "201831").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
      "a tax year before the earliest allowed date is supplied" in {
        val result: Either[ErrorWrapper, DeleteForeignReliefsRequestData] =
          validator(validNino, "2019-20").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
      "a tax year spanning an invalid tax year range is supplied" in {
        val result: Either[ErrorWrapper, DeleteForeignReliefsRequestData] =
          validator(validNino, "2020-22").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }
    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, DeleteForeignReliefsRequestData] =
          validator("invalid", "invalid").validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError))
          )
        )
      }
    }

  }

}
