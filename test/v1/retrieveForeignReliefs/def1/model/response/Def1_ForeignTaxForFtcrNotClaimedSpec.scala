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

package v1.retrieveForeignReliefs.def1.model.response

import play.api.libs.json.Json
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec

class Def1_ForeignTaxForFtcrNotClaimedSpec extends UnitSpec with JsonErrorValidators {

  val foreignTaxForFtcrNotClaimed: Def1_ForeignTaxForFtcrNotClaimed = Def1_ForeignTaxForFtcrNotClaimed(1429.99)

  val json = Json.parse(
    """{
      |  "amount": 1429.99
      |}""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        foreignTaxForFtcrNotClaimed shouldBe json.as[Def1_ForeignTaxForFtcrNotClaimed]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(foreignTaxForFtcrNotClaimed) shouldBe json
      }
    }
  }

}
