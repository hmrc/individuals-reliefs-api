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

package v1.models.response.retrieveReliefInvestments

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class SocialEnterpriseInvestmentItemSpec extends UnitSpec with JsonErrorValidators {
  val socialEnterpriseInvestmentItem = SocialEnterpriseInvestmentItem(
    Some("VCTREF"),
    Some("VCT Fund X"),
    Some("2018-04-16"),
    Some(BigDecimal(23312.00)),
    Some(BigDecimal(1334.00))
  )
  val json = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "VCTREF",
      |  "socialEnterpriseName": "VCT Fund X",
      |  "dateOfInvestment": "2018-04-16",
      |  "amountInvested": 23312.00,
      |  "reliefClaimed": 1334.00
      |}
        """.stripMargin
  )


  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[SocialEnterpriseInvestmentItem] shouldBe socialEnterpriseInvestmentItem
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(socialEnterpriseInvestmentItem) shouldBe json
      }
    }
  }
}