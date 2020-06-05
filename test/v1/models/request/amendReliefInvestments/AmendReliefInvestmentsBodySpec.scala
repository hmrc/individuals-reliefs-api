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

package v1.models.request.amendReliefInvestments

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class AmendReliefInvestmentsBodySpec extends UnitSpec with JsonErrorValidators {
  val amendReliefInvestmentsBody = AmendReliefInvestmentsBody(
    Some(Seq(VctSubscriptionsItem(
      Some("VCTREF"),
      Some("VCT Fund X"),
      Some("2018-04-16"),
      Some(BigDecimal(23312.00)),
      Some(BigDecimal(1334.00))
    ))),
    Some(Seq(EisSubscriptionsItem(
      Some("XTAL"),
      Some("EIS Fund X"),
      Some(true),
      Some("2020-12-12"),
      Some(BigDecimal(23312.00)),
      Some(BigDecimal(43432.00))
    ))),
    Some(Seq(CommunityInvestmentItem(
      Some("CIREF"),
      Some("CI X"),
      Some("2020-12-12"),
      Some(BigDecimal(6442.00)),
      Some(BigDecimal(2344.00))
    ))),
    Some(Seq(SeedEnterpriseInvestmentItem(
      Some("123412/1A"),
      Some("Company Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      Some(BigDecimal(3432.00))
    ))),
    Some(Seq(SocialEnterpriseInvestmentItem(
      Some("123412/1A"),
      Some("SE Inc"),
      Some("2020-12-12"),
      Some(BigDecimal(123123.22)),
      Some(BigDecimal(3432.00))
    )))
  )
  val json = Json.parse(
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
        """.stripMargin
  )


  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        amendReliefInvestmentsBody shouldBe json.as[AmendReliefInvestmentsBody]
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(amendReliefInvestmentsBody) shouldBe json
      }
    }
  }
  "isIncorrectOrEmptyBodyError" should {
    "return false" when {
      "all arrays are provided, none are empty, no objects in the arrays are empty" in {
        val model = AmendReliefInvestmentsBody(
          Some(Seq(VctSubscriptionsItem(
            Some("VCTREF"),
            Some("VCT Fund X"),
            Some("2018-04-16"),
            Some(BigDecimal(23312.00)),
            Some(BigDecimal(1334.00))
          ))),
          Some(Seq(EisSubscriptionsItem(
            Some("XTAL"),
            Some("EIS Fund X"),
            Some(true),
            Some("2020-12-12"),
            Some(BigDecimal(23312.00)),
            Some(BigDecimal(43432.00))
          ))),
          Some(Seq(CommunityInvestmentItem(
            Some("CIREF"),
            Some("CI X"),
            Some("2020-12-12"),
            Some(BigDecimal(6442.00)),
            Some(BigDecimal(2344.00))
          ))),
          Some(Seq(SeedEnterpriseInvestmentItem(
            Some("123412/1A"),
            Some("Company Inc"),
            Some("2020-12-12"),
            Some(BigDecimal(123123.22)),
            Some(BigDecimal(3432.00))
          ))),
          Some(Seq(SocialEnterpriseInvestmentItem(
            Some("123412/1A"),
            Some("SE Inc"),
            Some("2020-12-12"),
            Some(BigDecimal(123123.22)),
            Some(BigDecimal(3432.00))
          )))
        )
        model.isIncorrectOrEmptyBody shouldBe false
      }
      "only some arrays are provided, none are empty, no objects in the arrays are empty" in {
        val model = AmendReliefInvestmentsBody(
          None,
          Some(Seq(EisSubscriptionsItem(
            Some("XTAL"),
            Some("EIS Fund X"),
            Some(true),
            Some("2020-12-12"),
            Some(BigDecimal(23312.00)),
            Some(BigDecimal(43432.00))
          ))),
          Some(Seq(CommunityInvestmentItem(
            Some("CIREF"),
            Some("CI X"),
            Some("2020-12-12"),
            Some(BigDecimal(6442.00)),
            Some(BigDecimal(2344.00))
          ))),
          None,
          Some(Seq(SocialEnterpriseInvestmentItem(
            Some("123412/1A"),
            Some("SE Inc"),
            Some("2020-12-12"),
            Some(BigDecimal(123123.22)),
            Some(BigDecimal(3432.00))
          )))
        )
        model.isIncorrectOrEmptyBody shouldBe false
      }
    }
    "return true" when {
      "no arrays are provided" in {
        val model = AmendReliefInvestmentsBody(
          None,
          None,
          None,
          None,
          None
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
      "at least one empty array is provided" in {
        val model = AmendReliefInvestmentsBody(
          Some(Seq()),
          Some(Seq(EisSubscriptionsItem(
            Some("XTAL"),
            Some("EIS Fund X"),
            Some(true),
            Some("2020-12-12"),
            Some(BigDecimal(23312.00)),
            Some(BigDecimal(43432.00))
          ))),
          Some(Seq()),
          Some(Seq(SeedEnterpriseInvestmentItem(
            Some("123412/1A"),
            Some("Company Inc"),
            Some("2020-12-12"),
            Some(BigDecimal(123123.22)),
            Some(BigDecimal(3432.00))
          ))),
          Some(Seq(SocialEnterpriseInvestmentItem(
            Some("123412/1A"),
            Some("SE Inc"),
            Some("2020-12-12"),
            Some(BigDecimal(123123.22)),
            Some(BigDecimal(3432.00))
          )))
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
      "at least one array contains an empty object" in {
        val model = AmendReliefInvestmentsBody(
          Some(Seq(VctSubscriptionsItem(
            None,
            None,
            None,
            None,
            None
          ), VctSubscriptionsItem(
            Some("VCTREF"),
            Some("VCT Fund X"),
            Some("2018-04-16"),
            Some(BigDecimal(23312.00)),
            Some(BigDecimal(1334.00))
          ))),
          Some(Seq(EisSubscriptionsItem(
            Some("XTAL"),
            Some("EIS Fund X"),
            Some(true),
            Some("2020-12-12"),
            Some(BigDecimal(23312.00)),
            Some(BigDecimal(43432.00))
          ))),
          Some(Seq(CommunityInvestmentItem(
            Some("CIREF"),
            Some("CI X"),
            Some("2020-12-12"),
            Some(BigDecimal(6442.00)),
            Some(BigDecimal(2344.00))
          ))),
          Some(Seq(SeedEnterpriseInvestmentItem(
            Some("123412/1A"),
            Some("Company Inc"),
            Some("2020-12-12"),
            Some(BigDecimal(123123.22)),
            Some(BigDecimal(3432.00))
          ))),
          Some(Seq(SocialEnterpriseInvestmentItem(
            Some("123412/1A"),
            Some("SE Inc"),
            Some("2020-12-12"),
            Some(BigDecimal(123123.22)),
            Some(BigDecimal(3432.00))
          )))
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
    }
  }
}