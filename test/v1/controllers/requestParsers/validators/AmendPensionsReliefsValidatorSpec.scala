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

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors._
import v1.models.request.amendPensionsReliefs.AmendPensionsReliefsRawData

class AmendPensionsReliefsValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "pensionReliefs": {
      |    "regularPensionContributions": 1999.99,
      |    "oneOffPensionContributionsPaid": 1999.99,
      |    "retirementAnnuityPayments": 1999.99,
      |    "paymentToEmployersSchemeNoTaxRelief": 1999.99,
      |    "overseasPensionSchemeContributions": 1999.99
      |  }
      |}
      |""".stripMargin
  )
  private val requestBodyJsonNoDecimals = Json.parse(
    """
      |{
      |  "pensionReliefs": {
      |    "regularPensionContributions": 1999,
      |    "oneOffPensionContributionsPaid": 1999,
      |    "retirementAnnuityPayments": 1999,
      |    "paymentToEmployersSchemeNoTaxRelief": 1999,
      |    "overseasPensionSchemeContributions": 1999
      |  }
      |}
      |""".stripMargin
  )

  private val emptyJson = Json.parse(
    """
      |{}
      |""".stripMargin
  )


  val validator = new AmendPensionsReliefsValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied with decimal places in the JSON" in {
        validator.validate(AmendPensionsReliefsRawData(validNino, validTaxYear, requestBodyJson)) shouldBe Nil
      }
      "a valid request is supplied without decimal places in the JSON" in {
        validator.validate(AmendPensionsReliefsRawData(validNino, validTaxYear, requestBodyJsonNoDecimals)) shouldBe Nil
      }
    }

    "return a path parameter error" when {
      "the nino is invalid" in {
        validator.validate(AmendPensionsReliefsRawData("Walrus", validTaxYear, requestBodyJson)) shouldBe List(NinoFormatError)
      }
      "the taxYear format is invalid" in {
        validator.validate(AmendPensionsReliefsRawData(validNino, "2000", requestBodyJson)) shouldBe List(TaxYearFormatError)
      }
      "the taxYear range is invalid" in {
        validator.validate(AmendPensionsReliefsRawData(validNino, "2017-20", requestBodyJson)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "all path parameters are invalid" in {
        validator.validate(AmendPensionsReliefsRawData("Walrus", "2000", requestBodyJson)) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
    }

    "return a path rule error" when {
      "the tax year is too low" in {
        validator.validate(AmendPensionsReliefsRawData(validNino, "2018-19", requestBodyJson)) shouldBe List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in {
        validator.validate(AmendPensionsReliefsRawData(validNino, validTaxYear, emptyJson)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "an empty pensionReliefs object is provided" in {
        val json = Json.parse(
          """
            |{
            |  "pensionReliefs": {}
            |}
            |""".stripMargin)
        validator.validate(AmendPensionsReliefsRawData(validNino, validTaxYear, json)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }

    "return a FORMAT_VALUE error" when {
      Seq(
        "regularPensionContributions",
        "oneOffPensionContributionsPaid",
        "retirementAnnuityPayments",
        "paymentToEmployersSchemeNoTaxRelief",
        "overseasPensionSchemeContributions"
      ).foreach {
        value =>
          s"$value is provided" when {
            "value is below 0" in {
              val badJson = Json.parse(
                s"""
                  |{
                  |  "pensionReliefs": {
                  |    "$value": -1.00
                  |  }
                  |}
                  |""".stripMargin)
              validator.validate(AmendPensionsReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
                ValueFormatError.copy(paths = Some(Seq(
                  s"/pensionReliefs/$value"
                )))
              )
            }
            "value is greater than 99999999999.99" in {
              val tooLargeNumber: BigDecimal = 99999999999.99 + 0.01
              val badJson = Json.parse(
                s"""
                   |{
                   |  "pensionReliefs": {
                   |    "$value": $tooLargeNumber
                   |  }
                   |}
                   |""".stripMargin)
              validator.validate(AmendPensionsReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
                ValueFormatError.copy(paths = Some(Seq(
                  s"/pensionReliefs/$value"
                )))
              )
            }
            "value is greater than 3dp" in {
              val badJson = Json.parse(
                s"""
                   |{
                   |  "pensionReliefs": {
                   |    "$value": 1.123
                   |  }
                   |}
                   |""".stripMargin)
              validator.validate(AmendPensionsReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
                ValueFormatError.copy(paths = Some(Seq(
                  s"/pensionReliefs/$value"
                )))
              )
            }
          }
      }
    }
  }
}
