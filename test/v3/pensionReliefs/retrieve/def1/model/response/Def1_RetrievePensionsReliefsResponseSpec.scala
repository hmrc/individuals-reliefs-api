/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.pensionReliefs.retrieve.def1.model.response

import play.api.libs.json.*
import shared.models.domain.Timestamp
import shared.utils.UnitSpec
import v3.pensionReliefs.retrieve.model.response.RetrievePensionsReliefsResponse

class Def1_RetrievePensionsReliefsResponseSpec extends UnitSpec {

  val pensionsReliefsResponseItem: RetrievePensionsReliefsResponse = Def1_RetrievePensionsReliefsResponse(
    Timestamp("2019-04-04T01:01:01.000Z"),
    PensionsReliefs(
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99),
      Some(1999.99)
    )
  )

  val json: JsValue = Json.parse(
    """
      |{
      |  "submittedOn": "2019-04-04T01:01:01.000Z",
      |  "pensionReliefs": {
      |    "regularPensionContributions": 1999.99,
      |    "oneOffPensionContributionsPaid": 1999.99,
      |    "retirementAnnuityPayments": 1999.99,
      |    "paymentToEmployersSchemeNoTaxRelief": 1999.99,
      |    "overseasPensionSchemeContributions": 1999.99
      |  }
      |}
    """.stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[Def1_RetrievePensionsReliefsResponse] shouldBe pensionsReliefsResponseItem
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(pensionsReliefsResponseItem) shouldBe json
      }
    }
  }

  "error when JSON is invalid" in {
    JsObject.empty.validate[Def1_RetrievePensionsReliefsResponse] shouldBe a[JsError]
  }

}
