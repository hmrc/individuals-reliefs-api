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
import api.models.utils.JsonErrorValidators
import play.api.libs.json._
import support.UnitSpec
import v1.CreateAndAmendForeignReliefs.CreateAndAmendForeignReliefsValidatorFactory
import v1.CreateAndAmendForeignReliefs.def1.model.request.{Def1_CreateAndAmendForeignReliefsBody, Def1_CreateAndAmendForeignReliefsRequestData, Def1_ForeignIncomeTaxCreditRelief, Def1_ForeignTaxCreditRelief, Def1_ForeignTaxForFtcrNotClaimed}
import v1.CreateAndAmendForeignReliefs.model.request.CreateAndAmendForeignReliefsRequestData

class CreateAndAmendForeignReliefsValidatorFactorySpec extends UnitSpec with JsonErrorValidators {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  private def bodyWith(entries: JsValue*) = Json.parse(s"""
     |{
     |  "foreignTaxCreditRelief": {
     |    "amount": 1000.99
     |  },
     |  "foreignIncomeTaxCreditRelief": ${JsArray(entries)},
     |  "foreignTaxForFtcrNotClaimed": {
     |    "amount": 1400.99
     |  }
     |}
     |""".stripMargin)

  private val entry = Json.parse("""
      |{
      |  "countryCode": "FRA",
      |  "foreignTaxPaid": 1200.99,
      |  "taxableAmount": 1300.99,
      |  "employmentLumpSum": true
      |}
      |""".stripMargin)

  private val entryNoDecimals = Json.parse("""
       |{
       |  "countryCode": "FRA",
       |  "foreignTaxPaid": 1200,
       |  "taxableAmount": 1300,
       |  "employmentLumpSum": true
       |}
       |""".stripMargin)

  private val validBody = bodyWith(entry)

  private val validBodyNoDecimals =
    bodyWith(entryNoDecimals)
      .update("/foreignTaxCreditRelief/amount", JsNumber(1000))
      .update("/foreignTaxForFtcrNotClaimed/amount", JsNumber(1400))

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val parsedForeignTaxCreditRelief           = Def1_ForeignTaxCreditRelief(1000.99)
  private val parsedForeignTaxCreditReliefNoDecimals = Def1_ForeignTaxCreditRelief(1000)

  private val parsedForeignIncomeTaxCreditRelief =
    Def1_ForeignIncomeTaxCreditRelief("FRA", Some(1200.99), 1300.99, employmentLumpSum = true)

  private val parsedForeignIncomeTaxCreditReliefNoDecimals =
    Def1_ForeignIncomeTaxCreditRelief("FRA", Some(1200), 1300, employmentLumpSum = true)

  private val parsedForeignTaxForFtcrNotClaimed           = Def1_ForeignTaxForFtcrNotClaimed(1400.99)
  private val parsedForeignTaxForFtcrNotClaimedNoDecimals = Def1_ForeignTaxForFtcrNotClaimed(1400)

  private val parsedBody = Def1_CreateAndAmendForeignReliefsBody(
    Some(parsedForeignTaxCreditRelief),
    Some(List(parsedForeignIncomeTaxCreditRelief)),
    Some(parsedForeignTaxForFtcrNotClaimed))

  private val parsedBodyNoDecimals = Def1_CreateAndAmendForeignReliefsBody(
    Some(parsedForeignTaxCreditReliefNoDecimals),
    Some(List(parsedForeignIncomeTaxCreditReliefNoDecimals)),
    Some(parsedForeignTaxForFtcrNotClaimedNoDecimals)
  )

  private val validatorFactory = new CreateAndAmendForeignReliefsValidatorFactory

  private def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAndAmendForeignReliefsRequestData(parsedNino, parsedTaxYear, parsedBody))
      }

      "passed a valid request with no decimal places" in {
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, validTaxYear, validBodyNoDecimals).validateAndWrapResult()

        result shouldBe Right(Def1_CreateAndAmendForeignReliefsRequestData(parsedNino, parsedTaxYear, parsedBodyNoDecimals))
      }
    }

    "return a single error" when {
      "passed an invalid nino" in {
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator("invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "passed an invalidly formatted tax year" in {
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "passed an invalid tax year range" in {
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, "2019-21", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "passed a tax year that precedes the minimum" in {
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, "2019-20", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "passed an empty request body" in {
        val invalidBody = JsObject.empty
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))
      }

      "passed a body missing the mandatory amount field" in {
        val invalidBody = validBody.removeProperty("/foreignTaxCreditRelief/amount")
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/foreignTaxCreditRelief/amount")))
      }

      "passed a amount field where the value is below 0" when {
        List(
          "/foreignTaxCreditRelief/amount",
          "/foreignTaxForFtcrNotClaimed/amount"
        ).foreach(path =>
          s"for $path" in {
            val invalidBody = validBody.update(path, JsNumber(-1.00))
            val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
              validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

            result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(path)))
          })

        List(
          "/foreignTaxPaid",
          "/taxableAmount"
        ).foreach(pathInEntry =>
          s"for /foreignIncomeTaxCreditRelief/0$pathInEntry" in {
            val invalidBody = bodyWith(entry.update(pathInEntry, JsNumber(-1.00)))
            val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
              validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

            result shouldBe Left(ErrorWrapper(correlationId, ValueFormatError.withPath(s"/foreignIncomeTaxCreditRelief/0$pathInEntry")))
          })
      }

      "passed a body containing a country code that is too long" in {
        val invalidBody = bodyWith(entry.update("/countryCode", JsString("ABCD")))
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/foreignIncomeTaxCreditRelief/0/countryCode")))
      }

      "passed a body containing a country code that is too short" in {
        val invalidBody = bodyWith(entry.update("/countryCode", JsString("AB")))
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, CountryCodeFormatError.withPath("/foreignIncomeTaxCreditRelief/0/countryCode")))
      }

      "passed a body containing a country code that is not a valid ISO 3166-1 alpha-3 code" in {
        val invalidBody = bodyWith(entry.update("/countryCode", JsString("GER")))
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleCountryCodeError.withPath("/foreignIncomeTaxCreditRelief/0/countryCode")))
      }
    }
    "return multiple errors" when {
      "the request has multiple issues (path parameters)" in {
        val result: Either[ErrorWrapper, CreateAndAmendForeignReliefsRequestData] =
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
