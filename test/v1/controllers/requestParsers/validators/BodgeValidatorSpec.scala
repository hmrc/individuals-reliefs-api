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
import v1.models.requestData.amendReliefInvestments.AmendReliefInvestmentsRawData

class BodgeValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTaxYear = "2018-19"
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
      |      "uniqueInvestmentRef": "123412/1A",
      |      "companyName": "Company Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ],
      |  "socialEnterpriseInvestment": [
      |    {
      |      "uniqueInvestmentRef": "123412/1A",
      |      "socialEnterpriseName": "SE Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ]
      |}
        """.stripMargin)

  val validator = new BodgeValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, requestBodyJson)) shouldBe Nil
      }
    }

    "return every field in a FORMAT_VALUE error" when {
      "all fields are below 0" in {
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
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ValueFormatErrorGenerator.generate(Seq(
            "vctSubscription[0].amountInvested",
            "vctSubscription[0].reliefClaimed",
            "vctSubscription[1].amountInvested",
            "vctSubscription[1].reliefClaimed",
            "eisSubscription[0].amountInvested",
            "eisSubscription[0].reliefClaimed",
            "communityInvestment[0].amountInvested",
            "communityInvestment[0].reliefClaimed",
            "seedEnterpriseInvestment[0].amountInvested",
            "seedEnterpriseInvestment[0].reliefClaimed",
            "socialEnterpriseInvestment[0].amountInvested",
            "socialEnterpriseInvestment[0].reliefClaimed"
          ).sorted)
        )
      }
    }
    "return only some fields in a FORMAT_VALUE error" when {
      "only some fields are below 0" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": 1.00,
            |      "reliefClaimed": -1.00
            |      },
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": 1.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": 1.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": 1.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ValueFormatErrorGenerator.generate(Seq(
            "vctSubscription[0].reliefClaimed",
            "vctSubscription[1].amountInvested",
            "eisSubscription[0].amountInvested",
            "communityInvestment[0].reliefClaimed",
            "seedEnterpriseInvestment[0].amountInvested",
            "socialEnterpriseInvestment[0].reliefClaimed"
          ).sorted)
        )
      }
    }
  }
}
