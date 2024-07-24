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

package v1.pensionReliefs.createAmend.def1

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json.{JsNumber, JsObject, JsValue, Json}
import support.UnitSpec
import v1.pensionReliefs.createAmend.CreateAmendPensionsReliefsValidatorFactory
import v1.pensionReliefs.createAmend.def1.model.request.{CreateAmendPensionsReliefsBody, Def1_CreateAmendPensionsReliefsRequestData, PensionReliefs}
import v1.pensionReliefs.createAmend.model.request.CreateAmendPensionsReliefsRequestData

class Def1_CreateAmendPensionsReliefsValidatorSpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  implicit val correlationId: String = "X-12345"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

  private val validBody = Json.parse("""
      |{
      |  "pensionReliefs": {
      |    "regularPensionContributions": 1999.99,
      |    "oneOffPensionContributionsPaid": 1998.99,
      |    "retirementAnnuityPayments": 1997.99,
      |    "paymentToEmployersSchemeNoTaxRelief": 1996.99,
      |    "overseasPensionSchemeContributions": 1995.99
      |  }
      |}
      |""".stripMargin)

  private val validBodyNoDecimals = Json.parse("""
      |{
      |  "pensionReliefs": {
      |    "regularPensionContributions": 1999,
      |    "oneOffPensionContributionsPaid": 1998,
      |    "retirementAnnuityPayments": 1997,
      |    "paymentToEmployersSchemeNoTaxRelief": 1996,
      |    "overseasPensionSchemeContributions": 1995
      |  }
      |}
      |""".stripMargin)

  private val parsedNino    = Nino("AA123456A")
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedPensionsReliefs =
    PensionReliefs(Some(1999.99), Some(1998.99), Some(1997.99), Some(1996.99), Some(1995.99))

  private val parsedPensionsReliefsNoDecimals =
    PensionReliefs(Some(1999), Some(1998), Some(1997), Some(1996), Some(1995))

  private val parsedBody           = CreateAmendPensionsReliefsBody(parsedPensionsReliefs)
  private val parsedBodyNoDecimals = CreateAmendPensionsReliefsBody(parsedPensionsReliefsNoDecimals)

  private val validatorFactory = new CreateAmendPensionsReliefsValidatorFactory()

  private def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator(validNino, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendPensionsReliefsRequestData(parsedNino, parsedTaxYear, parsedBody))
      }

      "passed a valid request with no decimal places" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator(validNino, validTaxYear, validBodyNoDecimals).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAmendPensionsReliefsRequestData(parsedNino, parsedTaxYear, parsedBodyNoDecimals))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator("invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalidly formatted tax year" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator(validNino, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed an invalid tax year range" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator(validNino, "2019-21", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a tax year that precedes the minimum" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator(validNino, "2019-20", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed an empty request body" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator(validNino, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body containing an empty pensionReliefs object" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
          validator(validNino, validTaxYear, validBody.replaceWithEmptyObject("/pensionReliefs")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/pensionReliefs")))
      }

      def testWith(error: MtdError)(path: String, body: JsValue): Unit =
        s"for $path" in {
          val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
            validator(validNino, validTaxYear, body).validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      def testValueFormatErrorWith(path: String, body: JsValue): Unit =
        testWith(ValueFormatError.forPathAndRange(path, "0", "99999999999.99"))(path, body)

      val pensionReliefsPaths = List(
        "/pensionReliefs/regularPensionContributions",
        "/pensionReliefs/oneOffPensionContributionsPaid",
        "/pensionReliefs/retirementAnnuityPayments",
        "/pensionReliefs/paymentToEmployersSchemeNoTaxRelief",
        "/pensionReliefs/overseasPensionSchemeContributions"
      )

      "passed a body containing a value with an invalid format (negative value)" when {
        pensionReliefsPaths.foreach(path => testValueFormatErrorWith(path, validBody.update(path, JsNumber(-1.00))))
      }

      "passed a body containing a value greater than 99999999999.99" when {
        val tooLargeNumber: BigDecimal = 99999999999.99 + 0.01
        pensionReliefsPaths.foreach(path => testValueFormatErrorWith(path, validBody.update(path, JsNumber(tooLargeNumber))))
      }

      "passed a body with a value with 3 decimal places" when {
        pensionReliefsPaths.foreach(path => testValueFormatErrorWith(path, validBody.update(path, JsNumber(1.123))))
      }
    }
    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, CreateAmendPensionsReliefsRequestData] =
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
