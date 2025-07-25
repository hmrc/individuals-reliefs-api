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

package v3.foreignReliefs.retrieve.def1.model.response

import play.api.libs.json.Json
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec

class Def1_ForeignIncomeTaxCreditReliefSpec extends UnitSpec with JsonErrorValidators {

  val foreignIncomeTaxCreditRelief: Def1_ForeignIncomeTaxCreditRelief = Def1_ForeignIncomeTaxCreditRelief("FRA", Some(640.32), 204.78, false)

  val json = Json.parse(
    """{
      |  "countryCode": "FRA",
      |  "foreignTaxPaid": 640.32,
      |  "taxableAmount": 204.78,
      |  "employmentLumpSum": false
      |}""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        foreignIncomeTaxCreditRelief shouldBe json.as[Def1_ForeignIncomeTaxCreditRelief]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignIncomeTaxCreditRelief) shouldBe json
      }
    }
  }

}
