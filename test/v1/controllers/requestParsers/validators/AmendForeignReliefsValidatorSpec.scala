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
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors._
import v1.models.request.amendForeignReliefs.AmendForeignReliefsRawData

class AmendForeignReliefsValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "foreignTaxCreditRelief": {
      |    "amount": 763.25
      |  }
      |}
      |""".stripMargin
  )
  private val requestBodyJsonNoDecimals = Json.parse(
    """
      |{
      |  "foreignTaxCreditRelief": {
      |    "amount": 763
      |  }
      |}
      |""".stripMargin
  )

  private val emptyJson = Json.parse(
    """
      |{}
      |""".stripMargin
  )


  class Test {
    val validator = new AmendForeignReliefsValidator(mockAppConfig)
  }

  "running a validation" should {

    "return no errors" when {
      "a valid request is supplied with decimal places in the JSON" in new Test {
        validator.validate(AmendForeignReliefsRawData(validNino, validTaxYear, requestBodyJson)) shouldBe Nil
      }
      "a valid request is supplied without decimal places in the JSON" in new Test {
        validator.validate(AmendForeignReliefsRawData(validNino, validTaxYear, requestBodyJsonNoDecimals)) shouldBe Nil
      }
    }

    "return a path parameter error" when {
      "the nino is invalid" in new Test {
        validator.validate(AmendForeignReliefsRawData("Walrus", validTaxYear, requestBodyJson)) shouldBe List(NinoFormatError)
      }
      "the taxYear format is invalid" in new Test {
        validator.validate(AmendForeignReliefsRawData(validNino, "2000", requestBodyJson)) shouldBe List(TaxYearFormatError)
      }
      "the taxYear range is invalid" in new Test {
        validator.validate(AmendForeignReliefsRawData(validNino, "2017-20", requestBodyJson)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "the taxYear is too early" in new Test {
        validator.validate(AmendForeignReliefsRawData(validNino, "2019-20", requestBodyJson)) shouldBe List(RuleTaxYearNotSupportedError)
      }
      "all path parameters are invalid" in new Test {
        validator.validate(AmendForeignReliefsRawData("Walrus", "2000", requestBodyJson)) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendForeignReliefsRawData(validNino, validTaxYear, emptyJson)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "at least one mandatory field is missing" in new Test {
        val json = Json.parse(
            """
              |{
              |  "foreignTaxCreditRelief": {}
              |}
              |""".stripMargin)
        validator.validate(AmendForeignReliefsRawData(validNino, validTaxYear, json)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }

    "return a FORMAT_VALUE error" when {
      "amount is below 0" in new Test {
        val badJson = Json.parse(
          """
            |{
            |  "foreignTaxCreditRelief": {
            |    "amount": -1.00
            |  }
            |}
            |""".stripMargin)
        validator.validate(AmendForeignReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq(
            "/foreignTaxCreditRelief/amount"
          )))
        )
      }
    }
  }
}
