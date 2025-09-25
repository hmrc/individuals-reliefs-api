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
import shared.utils.UnitSpec

class PensionsReliefsSpec extends UnitSpec {

  val pensionsReliefsItem: PensionsReliefs = PensionsReliefs(
    Some(1999.99),
    Some(1999.99),
    Some(1999.99),
    Some(1999.99),
    Some(1999.99)
  )

  val json: JsValue = Json.parse(
    """
      |{
      |  "regularPensionContributions": 1999.99,
      |  "oneOffPensionContributionsPaid": 1999.99,
      |  "retirementAnnuityPayments": 1999.99,
      |  "paymentToEmployersSchemeNoTaxRelief": 1999.99,
      |  "overseasPensionSchemeContributions": 1999.99
      |}
      """.stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[PensionsReliefs] shouldBe pensionsReliefsItem
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(pensionsReliefsItem) shouldBe json
      }
    }
  }

  "error when JSON is invalid" in {
    val invalidJson = Json.obj(
      "regularPensionContributions" -> Json.arr()
    )
    invalidJson.validate[PensionsReliefs] shouldBe a[JsError]
  }

}
