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
import v1.models.request.amendReliefInvestments.AmendReliefInvestmentsRawData

class AmendReliefInvestmentValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "vctSubscription":[
      |    {
      |      "uniqueInvestmentRef": "VCTREF",
      |      "name": "VCT Fund X",
      |      "dateOfInvestment": "2018-04-16",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 1334.00
      |      }
      |  ],
      |  "eisSubscription":[
      |    {
      |      "uniqueInvestmentRef": "XTAL",
      |      "name": "EIS Fund X",
      |      "knowledgeIntensive": true,
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 43432.00
      |    }
      |  ],
      |  "communityInvestment": [
      |    {
      |      "uniqueInvestmentRef": "CIREF",
      |      "name": "CI X",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 6442.00,
      |      "reliefClaimed": 2344.00
      |    }
      |  ],
      |  "seedEnterpriseInvestment": [
      |    {
      |      "uniqueInvestmentRef": "1234121A",
      |      "companyName": "Company Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ],
      |  "socialEnterpriseInvestment": [
      |    {
      |      "uniqueInvestmentRef": "1234121A",
      |      "socialEnterpriseName": "SE Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ]
      |}
        """.stripMargin)

  class Test {
    val validator = new AmendReliefInvestmentValidator(mockAppConfig)
    MockedAppConfig.reliefsMinimumTaxYear returns 2022 anyNumberOfTimes()
  }
    
  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, requestBodyJson)) shouldBe Nil
      }
    }

    "return FORMAT_NINO error" when {
      "a bad nino is provided" in new Test {
        validator.validate(AmendReliefInvestmentsRawData("BALONEY", validTaxYear, requestBodyJson)) shouldBe List(NinoFormatError)
      }
    }

    "return FORMAT_TAX_YEAR error" when {
      "a bad tax year is provided" in new Test {
        validator.validate(AmendReliefInvestmentsRawData(validNino, "BALONEY", requestBodyJson)) shouldBe List(TaxYearFormatError)
      }
    }

    "return RULE_TAX_YEAR_NOT_SUPPORTED error" when {
      "a tax year before the earliest allowed date is supplied" in new Test {
        validator.validate(AmendReliefInvestmentsRawData(validNino, "2020-21", requestBodyJson)) shouldBe List(RuleTaxYearNotSupportedError)
      }
    }

    "return RULE_TAX_YEAR_RANGE_INVALID error" when {
      "an invalid tax year range is provided" in new Test {
        validator.validate(AmendReliefInvestmentsRawData(validNino, "2021-23", requestBodyJson)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
    }

    "return a FORMAT_VALUE error" when {
      "all fields are below 0" in new Test {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |      },
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq(
            "/vctSubscription/0/amountInvested",
            "/vctSubscription/0/reliefClaimed",
            "/vctSubscription/1/amountInvested",
            "/vctSubscription/1/reliefClaimed",
            "/eisSubscription/0/amountInvested",
            "/eisSubscription/0/reliefClaimed",
            "/communityInvestment/0/amountInvested",
            "/communityInvestment/0/reliefClaimed",
            "/seedEnterpriseInvestment/0/amountInvested",
            "/seedEnterpriseInvestment/0/reliefClaimed",
            "/socialEnterpriseInvestment/0/amountInvested",
            "/socialEnterpriseInvestment/0/reliefClaimed"
          )))
        )
      }
    }

    "return RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
      "no JSON fields are provided" in new Test {
        val json = Json.parse("""{}""".stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, json)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "at least one empty array is provided" in new Test {
        val json = Json.parse(
          """
            |{
            |  "vctSubscription":[],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, json)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "at least one array contains an empty object" in new Test {
        val json = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {}
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, json)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }

    "return a FORMAT_DATE_OF_INVESTMENT error" when {
      "provided dates are invalid" in new Test {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)

        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          DateOfInvestmentFormatError.copy(paths = Some(Seq(
            "/vctSubscription/0/dateOfInvestment",
            "/eisSubscription/0/dateOfInvestment",
            "/communityInvestment/0/dateOfInvestment",
            "/seedEnterpriseInvestment/0/dateOfInvestment",
            "/socialEnterpriseInvestment/0/dateOfInvestment"
          )))
        )
      }
    }

    "return a FORMAT_UNIQUE_INVESTMENT_REFERENCE error" when {
      "provided unique investment references are invalid" in new Test {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "ABC/123",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "ABC/123",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "ABC/123",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "ABC/123",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "ABC/123",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)

        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          UniqueInvestmentRefFormatError.copy(paths = Some(Seq(
            "/vctSubscription/0/uniqueInvestmentRef",
            "/eisSubscription/0/uniqueInvestmentRef",
            "/communityInvestment/0/uniqueInvestmentRef",
            "/seedEnterpriseInvestment/0/uniqueInvestmentRef",
            "/socialEnterpriseInvestment/0/uniqueInvestmentRef"
          )))
        )
      }
    }

    "return a FORMAT_NAME error" when {
      "provided names are invalid" in new Test {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "companyName": "",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "1234121A",
            |      "socialEnterpriseName": "",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)

        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          NameFormatError.copy(paths = Some(Seq(
            "/vctSubscription/0/name",
            "/eisSubscription/0/name",
            "/communityInvestment/0/name",
            "/seedEnterpriseInvestment/0/companyName",
            "/socialEnterpriseInvestment/0/socialEnterpriseName"
          )))
        )
      }
    }
  }
}