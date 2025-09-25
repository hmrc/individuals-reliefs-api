/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.foreignReliefs.retrieve.def1.model.response

import play.api.libs.json.*
import shared.utils.UnitSpec

class Def1_ForeignTaxCreditReliefSpec extends UnitSpec {

  val foreignTaxCreditRelief: Def1_ForeignTaxCreditRelief = Def1_ForeignTaxCreditRelief(2314.32)

  val json = Json.parse(
    """{
      |  "amount": 2314.32
      |}""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        foreignTaxCreditRelief shouldBe json.as[Def1_ForeignTaxCreditRelief]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignTaxCreditRelief) shouldBe json
      }
    }
  }

  "error when JSON is invalid" in {
    JsObject.empty.validate[Def1_ForeignTaxCreditRelief] shouldBe a[JsError]
  }

}
