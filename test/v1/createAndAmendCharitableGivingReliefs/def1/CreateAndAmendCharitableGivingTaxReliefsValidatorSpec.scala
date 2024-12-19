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

package v1.createAndAmendCharitableGivingReliefs.def1

import common.{RuleGiftAidNonUkAmountWithoutNamesError, RuleGiftsNonUkAmountWithoutNamesError}
import play.api.libs.json._
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v1.createAndAmendCharitableGivingReliefs.CreateAndAmendCharitableGivingReliefsValidatorFactory
import v1.createAndAmendCharitableGivingReliefs.def1.model.request._
import v1.createAndAmendCharitableGivingReliefs.model.request.{
  CreateAndAmendCharitableGivingTaxReliefsRequestData,
  Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData
}

class CreateAndAmendCharitableGivingTaxReliefsValidatorSpec extends UnitSpec with JsonErrorValidators {
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validNino    = "ZG903729C"
  private val validTaxYear = "2020-21"

  private def bodyWith(charityNames1: Seq[String], charityNames2: Seq[String]) =
    Json.parse(s"""
    |{
                                                                                               |   "giftAidPayments": {
                                                                                               |     "nonUkCharities": {
                                                                                               |       "charityNames": ${JsArray(
                   charityNames1.map(JsString))},
                                                                                               |       "totalAmount": 10.99
                                                                                               |     },
                                                                                               |     "totalAmount": 11.99,
                                                                                               |     "oneOffAmount": 12.99,
                                                                                               |     "amountTreatedAsPreviousTaxYear": 13.99,
                                                                                               |     "amountTreatedAsSpecifiedTaxYear": 14.99
                                                                                               |   },
                                                                                               |   "gifts": {
                                                                                               |     "nonUkCharities": {
                                                                                               |       "charityNames": ${JsArray(
                   charityNames2.map(JsString))},
                                                                                               |       "totalAmount": 15.99
                                                                                               |     },
                                                                                               |     "landAndBuildings": 16.99,
                                                                                               |     "sharesOrSecurities": 17.99
                                                                                               |   }
                                                                                               |}""".stripMargin)

  private val validCompanyName1 = "Company name 1"
  private val validCompanyName2 = "Company name 2"

  private val validBody = bodyWith(List(validCompanyName1), List(validCompanyName2))

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedNonUkCharities1 = Def1_NonUkCharities(Some(List(validCompanyName1)), 10.99)
  private val parsedNonUkCharities2 = Def1_NonUkCharities(Some(List(validCompanyName2)), 15.99)
  private val parsedGiftAidPayments = Def1_GiftAidPayments(Some(parsedNonUkCharities1), Some(11.99), Some(12.99), Some(13.99), Some(14.99))
  private val parsedGifts           = Def1_Gifts(Some(parsedNonUkCharities2), Some(16.99), Some(17.99))

  private val parsedBody = Def1_CreateAndAmendCharitableGivingTaxReliefsBody(Some(parsedGiftAidPayments), Some(parsedGifts))

  private val validatorFactory = new CreateAndAmendCharitableGivingReliefsValidatorFactory()

  private def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData(parsedNino, parsedTaxYear, parsedBody))
      }

      "passed a valid request with only gift payments included" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, validBody.removeProperty("/gifts")).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData(parsedNino, parsedTaxYear, parsedBody.copy(gifts = None)))
      }

      "passed a valid request with only gifts included" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, validBody.removeProperty("/giftAidPayments")).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData(parsedNino, parsedTaxYear, parsedBody.copy(giftAidPayments = None)))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator("invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalidly formatted tax year" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed an invalid tax year" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, "2016-17", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed a tax year with an invalid range" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, "2018-20", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a body with an invalid numeric field" when {
        def testValueFormatError(path: String): Unit = s"for $path" in {
          val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
            validator(validNino, validTaxYear, validBody.update(path, JsNumber(123.456))).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(path)))
        }

        List(
          "/giftAidPayments/nonUkCharities/totalAmount",
          "/giftAidPayments/totalAmount",
          "/giftAidPayments/oneOffAmount",
          "/giftAidPayments/amountTreatedAsPreviousTaxYear",
          "/giftAidPayments/amountTreatedAsSpecifiedTaxYear",
          "/gifts/nonUkCharities/totalAmount",
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
        val invalidBody = validBody.replaceWithEmptyObject("/giftAidPayments")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/giftAidPayments")))
      }

      "passed a body containing an empty gifts object" in {
        val invalidBody = validBody.replaceWithEmptyObject("/gifts")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/gifts")))
      }

      "passed a body containing a giftAidPayments object missing a mandatory field" in {
        val invalidBody = validBody.removeProperty("/giftAidPayments/nonUkCharities/totalAmount")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/giftAidPayments/nonUkCharities/totalAmount")))
      }

      "passed a body containing a giftAidPayments object with an empty charity names array" in {
        val invalidBody = validBody.update("/giftAidPayments/nonUkCharities/charityNames", JsArray(List()))
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/giftAidPayments/nonUkCharities/charityNames")))
      }

      "passed a body containing a gifts object missing a mandatory field" in {
        val invalidBody = validBody.removeProperty("/gifts/nonUkCharities/totalAmount")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/gifts/nonUkCharities/totalAmount")))
      }

      "passed a body containing a gifts object with an empty charity names array" in {
        val invalidBody = validBody.update("/gifts/nonUkCharities/charityNames", JsArray(List()))
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/gifts/nonUkCharities/charityNames")))
      }

      "passed a body containing a giftAidPayments object missing the charityNames field" in {
        val invalidBody = validBody.removeProperty("/giftAidPayments/nonUkCharities/charityNames")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGiftAidNonUkAmountWithoutNamesError))
      }

      "passed a body containing a gifts object missing the charityNames field" in {
        val invalidBody = validBody.removeProperty("/gifts/nonUkCharities/charityNames")
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleGiftsNonUkAmountWithoutNamesError))
      }

      "passed a body containing a giftAidPayments object containing invalidly formatted charity names" in {
        val invalidBody = bodyWith(List("Good", "X" * 76, "Y" * 76), List(validCompanyName2))
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            StringFormatError.withPaths(
              List(
                "/giftAidPayments/nonUkCharities/charityNames/1",
                "/giftAidPayments/nonUkCharities/charityNames/2"
              ))))
      }

      "passed a body containing a gifts object containing invalidly formatted charity names" in {
        val invalidBody = bodyWith(List(validCompanyName1), List("Good", "X" * 76, "Y" * 76))
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            StringFormatError.withPaths(
              List(
                "/gifts/nonUkCharities/charityNames/1",
                "/gifts/nonUkCharities/charityNames/2"
              ))))
      }
    }

    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, CreateAndAmendCharitableGivingTaxReliefsRequestData] =
          validator("invalid", "invalid", validBody).validateAndWrapResult()

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
