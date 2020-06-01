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
import v1.models.errors.{FormatDateOfInvestmentErrorGenerator, FormatInvestmentRefErrorGenerator, FormatNameErrorGenerator, ValueFormatErrorGenerator}
import v1.models.requestData.amendReliefInvestments.AmendReliefInvestmentsRawData

class AmendReliefInvestmentValidatorSpec extends UnitSpec {

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

  val validator = new AmendReliefInvestmentValidator()

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
            "vctSubscription/[0]/amountInvested",
            "vctSubscription/[0]/reliefClaimed",
            "vctSubscription/[1]/amountInvested",
            "vctSubscription/[1]/reliefClaimed",
            "eisSubscription/[0]/amountInvested",
            "eisSubscription/[0]/reliefClaimed",
            "communityInvestment/[0]/amountInvested",
            "communityInvestment/[0]/reliefClaimed",
            "seedEnterpriseInvestment/[0]/amountInvested",
            "seedEnterpriseInvestment/[0]/reliefClaimed",
            "socialEnterpriseInvestment/[0]/amountInvested",
            "socialEnterpriseInvestment/[0]/reliefClaimed"
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
            "vctSubscription/[0]/reliefClaimed",
            "vctSubscription/[1]/amountInvested",
            "eisSubscription/[0]/amountInvested",
            "communityInvestment/[0]/reliefClaimed",
            "seedEnterpriseInvestment/[0]/amountInvested",
            "socialEnterpriseInvestment/[0]/reliefClaimed"
          ).sorted)
        )
      }
    }
    "return a format date of investment error with multiple incorrect date of investments" when {
      "the provided date of investment's format is incorrect" in {
        val badJson =Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          FormatDateOfInvestmentErrorGenerator.generate(Seq(
            "vctSubscription/[0]/dateOfInvestment",
            "eisSubscription/[0]/dateOfInvestment",
            "communityInvestment/[0]/dateOfInvestment",
            "seedEnterpriseInvestment/[0]/dateOfInvestment",
            "socialEnterpriseInvestment/[0]/dateOfInvestment"
          ).sorted)
        )
      }
    }
    "return a format name error with multiple incorrect names" when {
      "the provided name's formats are incorrect" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "AA1234*&^%$£BBCBCBC",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "AA1234*&^%$£BBCBCBC",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "AA1234*&^%$£BBCBCBC",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "AA1234*&^%$£BBCBCBC",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "socialEnterpriseName": "AA1234*&^%$£BBCBCBC",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          FormatNameErrorGenerator.generate(Seq(
            "vctSubscription/[0]/name",
            "eisSubscription/[0]/name",
            "communityInvestment/[0]/name",
            "seedEnterpriseInvestment/[0]/companyName",
            "socialEnterpriseInvestment/[0]/socialEnterpriseName"
          ).sorted)
        )
      }
    }
    "return a unique investment reference error" when {
      "the provided unique investment reference is incorrect" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestment": [
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          FormatInvestmentRefErrorGenerator.generate(Seq(
           "vctSubscription/[0]/uniqueInvestmentRef",
           "eisSubscription/[0]/uniqueInvestmentRef",
           "communityInvestment/[0]/uniqueInvestmentRef",
           "seedEnterpriseInvestment/[0]/uniqueInvestmentRef",
           "socialEnterpriseInvestment/[0]/uniqueInvestmentRef"
          ).sorted)
        )
      }
    }
    "return a multiple different errors" when {
      "the provided data has multiple different errors" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscription":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscription":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "12-12-2018",
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
            |      "reliefClaimed": 0
            |    }
            |  ],
            |  "socialEnterpriseInvestment": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 0,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ValueFormatErrorGenerator.generate(Seq(
            "seedEnterpriseInvestment/[0]/reliefClaimed",
            "socialEnterpriseInvestment/[0]/amountInvested"
          ).sorted),
          FormatDateOfInvestmentErrorGenerator.generate(Seq(
            "vctSubscription/[0]/dateOfInvestment",
            "eisSubscription/[0]/dateOfInvestment"
          ).sorted)
        )
      }
    }
  }
}
