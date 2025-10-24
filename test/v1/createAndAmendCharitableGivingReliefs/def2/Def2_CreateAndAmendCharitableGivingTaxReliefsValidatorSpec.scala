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

package v1.createAndAmendCharitableGivingReliefs.def2

import play.api.libs.json.*
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v1.createAndAmendCharitableGivingReliefs.CreateAndAmendCharitableGivingReliefsValidatorFactory
import v1.createAndAmendCharitableGivingReliefs.def2.model.request.*
import v1.createAndAmendCharitableGivingReliefs.model.request.{
  CreateAndAmendCharitableGivingTaxReliefsRequestData,
  Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData
}
import v1.fixtures.createAndAmendCharitableGivingTaxReliefs.Def2_CreateAndAmendCharitableGivingTaxReliefsFixtures.{model, mtdJson}

class Def2_CreateAndAmendCharitableGivingTaxReliefsValidatorSpec extends UnitSpec with JsonErrorValidators {
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validNino    = "ZG903729C"
  private val validTaxYear = "2025-26"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validatorFactory = new CreateAndAmendCharitableGivingReliefsValidatorFactory()

  private def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, mtdJson).validateAndWrapResult()

        result shouldBe Right(Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData(parsedNino, parsedTaxYear, model))
      }

      "passed a valid request with only gift payments included" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, mtdJson.removeProperty("/gifts")).validateAndWrapResult()

        result shouldBe Right(Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData(parsedNino, parsedTaxYear, model.copy(gifts = None)))
      }

      "passed a valid request with only gifts included" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, mtdJson.removeProperty("/giftAidPayments")).validateAndWrapResult()

        result shouldBe Right(Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData(parsedNino, parsedTaxYear, model.copy(giftAidPayments = None)))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator("invalid", validTaxYear, mtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalidly formatted tax year" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, "invalid", mtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed an invalid tax year" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, "2016-17", mtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a tax year with an invalid range" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, "2018-20", mtdJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a body with an invalid numeric field" when {
        def testValueFormatError(path: String): Unit = s"for $path" in {
          val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
            validator(validNino, validTaxYear, mtdJson.update(path, JsNumber(123.456))).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(path)))
        }

        List(
          "/giftAidPayments/totalAmount",
          "/giftAidPayments/oneOffAmount",
          "/giftAidPayments/amountTreatedAsPreviousTaxYear",
          "/giftAidPayments/amountTreatedAsSpecifiedTaxYear",
          "/gifts/landAndBuildings",
          "/gifts/sharesOrSecurities"
        ).foreach(testValueFormatError)
      }

      "passed an empty body" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body containing an empty giftAidPayments object" in {
        val invalidBody = mtdJson.replaceWithEmptyObject("/giftAidPayments")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/giftAidPayments")))
      }

      "passed a body containing an empty gifts object" in {
        val invalidBody = mtdJson.replaceWithEmptyObject("/gifts")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/gifts")))
      }
    }
  }

}
