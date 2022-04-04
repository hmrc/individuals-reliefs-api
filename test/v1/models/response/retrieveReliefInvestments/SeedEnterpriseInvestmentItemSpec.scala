/*
 * Copyright 2022 HM Revenue & Customs
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

class SeedEnterpriseInvestmentItemSpec extends UnitSpec with JsonErrorValidators {

  val seedEnterpriseInvestmentItem: SeedEnterpriseInvestmentItem = SeedEnterpriseInvestmentItem(
    "123412/1A",
    Some("Company Inc"),
    Some("2020-12-12"),
    Some(BigDecimal(123123.22)),
    BigDecimal(3432.00)
  )

  val json = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "123412/1A",
      |  "companyName": "Company Inc",
      |  "dateOfInvestment": "2020-12-12",
      |  "amountInvested": 123123.22,
      |  "reliefClaimed": 3432.00
      |}
        """.stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[SeedEnterpriseInvestmentItem] shouldBe seedEnterpriseInvestmentItem
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(seedEnterpriseInvestmentItem) shouldBe json
      }
    }
  }

}
