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
import v1.models.errors.{FormatDateOfInvestmentErrorGenerator, FormatInvestmentRefErrorGenerator, FormatNameErrorGenerator, FormatValueErrorGenerator}
import v1.models.requestData.amendReliefInvestments.AmendReliefInvestmentsRawData

class AmendReliefInvestmentValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTaxYear = "2018-19"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "vctSubscriptionsItems":[
      |    {
      |      "uniqueInvestmentRef": "VCTREF",
      |      "name": "VCT Fund X",
      |      "dateOfInvestment": "2018-04-16",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 1334.00
      |      }
      |  ],
      |  "eisSubscriptionsItems":[
      |    {
      |      "uniqueInvestmentRef": "XTAL",
      |      "name": "EIS Fund X",
      |      "knowledgeIntensive": true,
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 23312.00,
      |      "reliefClaimed": 43432.00
      |    }
      |  ],
      |  "communityInvestmentItems": [
      |    {
      |      "uniqueInvestmentRef": "CIREF",
      |      "name": "CI X",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 6442.00,
      |      "reliefClaimed": 2344.00
      |    }
      |  ],
      |  "seedEnterpriseInvestmentItems": [
      |    {
      |      "uniqueInvestmentRef": "123412/1A",
      |      "companyName": "Company Inc",
      |      "dateOfInvestment": "2020-12-12",
      |      "amountInvested": 123123.22,
      |      "reliefClaimed": 3432.00
      |    }
      |  ],
      |  "socialEnterpriseInvestmentItems": [
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
            |  "vctSubscriptionsItems":[
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
            |  "eisSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "communityInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "seedEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "socialEnterpriseInvestmentItems": [
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
          FormatValueErrorGenerator.generate(Seq(
            "/vctSubscriptionsItems/0/amountInvested",
            "/vctSubscriptionsItems/0/reliefClaimed",
            "/vctSubscriptionsItems/1/amountInvested",
            "/vctSubscriptionsItems/1/reliefClaimed",
            "/eisSubscriptionsItems/0/amountInvested",
            "/eisSubscriptionsItems/0/reliefClaimed",
            "/communityInvestmentItems/0/amountInvested",
            "/communityInvestmentItems/0/reliefClaimed",
            "/seedEnterpriseInvestmentItems/0/amountInvested",
            "/seedEnterpriseInvestmentItems/0/reliefClaimed",
            "/socialEnterpriseInvestmentItems/0/amountInvested",
            "/socialEnterpriseInvestmentItems/0/reliefClaimed"
          ))
        )
      }
    }
    "return only some fields in a FORMAT_VALUE error" when {
      "only some fields are below 0" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscriptionsItems":[
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
            |  "eisSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": 1.00
            |    }
            |  ],
            |  "communityInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 1.00,
            |      "reliefClaimed": -1.00
            |    }
            |  ],
            |  "seedEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": -1.00,
            |      "reliefClaimed": 1.00
            |    }
            |  ],
            |  "socialEnterpriseInvestmentItems": [
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
          FormatValueErrorGenerator.generate(Seq(
            "/vctSubscriptionsItems/0/reliefClaimed",
            "/vctSubscriptionsItems/1/amountInvested",
            "/eisSubscriptionsItems/0/amountInvested",
            "/communityInvestmentItems/0/reliefClaimed",
            "/seedEnterpriseInvestmentItems/0/amountInvested",
            "/socialEnterpriseInvestmentItems/0/reliefClaimed"
          ))
        )
      }
    }
    "return a format date of investment error with multiple incorrect date of investments" when {
      "the provided date of investment's format is incorrect" in {
        val badJson =Json.parse(
          """
            |{
            |  "vctSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestmentItems": [
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
            "/vctSubscriptionsItems/0/dateOfInvestment",
            "/eisSubscriptionsItems/0/dateOfInvestment",
            "/communityInvestmentItems/0/dateOfInvestment",
            "/seedEnterpriseInvestmentItems/0/dateOfInvestment",
            "/socialEnterpriseInvestmentItems/0/dateOfInvestment"
          ))
        )
      }
    }
    "return a format name error with multiple incorrect names" when {
      "the provided name's formats are incorrect" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "AA1234*&^%$£BBCBCBC",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "AA1234*&^%$£BBCBCBC",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "AA1234*&^%$£BBCBCBC",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestmentItems": [
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
            "/vctSubscriptionsItems/0/name",
            "/eisSubscriptionsItems/0/name",
            "/communityInvestmentItems/0/name",
            "/seedEnterpriseInvestmentItems/0/companyName",
            "/socialEnterpriseInvestmentItems/0/socialEnterpriseName"
          ))
        )
      }
    }
    "return a unique investment reference error" when {
      "the provided unique investment reference is incorrect" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "2018-04-16",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "bad***ref",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ],
            |  "socialEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "bad ref",
            |      "socialEnterpriseName": "SE Inc",
            |      "dateOfInvestment": "",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 3432.00
            |    }
            |  ]
            |}
        """.stripMargin)
        validator.validate(AmendReliefInvestmentsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          FormatInvestmentRefErrorGenerator.generate(Seq(
            "/vctSubscriptionsItems/0/uniqueInvestmentRef",
            "/eisSubscriptionsItems/0/uniqueInvestmentRef",
            "/communityInvestmentItems/0/uniqueInvestmentRef",
            "/seedEnterpriseInvestmentItems/0/uniqueInvestmentRef",
            "/socialEnterpriseInvestmentItems/0/uniqueInvestmentRef"
          ))
        )
      }
    }
    "return a multiple different errors" when {
      "the provided data has multiple different errors" in {
        val badJson = Json.parse(
          """
            |{
            |  "vctSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "VCTREF",
            |      "name": "VCT Fund X",
            |      "dateOfInvestment": "04-16-2018",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 1334.00
            |      }
            |  ],
            |  "eisSubscriptionsItems":[
            |    {
            |      "uniqueInvestmentRef": "XTAL",
            |      "name": "EIS Fund X",
            |      "knowledgeIntensive": true,
            |      "dateOfInvestment": "12-12-2018",
            |      "amountInvested": 23312.00,
            |      "reliefClaimed": 43432.00
            |    }
            |  ],
            |  "communityInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "CIREF",
            |      "name": "CI X",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 6442.00,
            |      "reliefClaimed": 2344.00
            |    }
            |  ],
            |  "seedEnterpriseInvestmentItems": [
            |    {
            |      "uniqueInvestmentRef": "123412/1A",
            |      "companyName": "Company Inc",
            |      "dateOfInvestment": "2020-12-12",
            |      "amountInvested": 123123.22,
            |      "reliefClaimed": 0
            |    }
            |  ],
            |  "socialEnterpriseInvestmentItems": [
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
          FormatValueErrorGenerator.generate(Seq(
            "/seedEnterpriseInvestmentItems/0/reliefClaimed",
            "/socialEnterpriseInvestmentItems/0/amountInvested"
          )),
          FormatDateOfInvestmentErrorGenerator.generate(Seq(
            "/vctSubscriptionsItems/0/dateOfInvestment",
            "/eisSubscriptionsItems/0/dateOfInvestment"
          ))
        )
      }
    }
  }
}