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

package v1.models.request.CreateAndAmendCharitableGivingTaxRelief

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.request.createAndAmendCharitableGivingTaxRelief.NonUkCharities

class NonUkCharitiesSpec extends UnitSpec {

  val model: NonUkCharities =
    NonUkCharities(
      charityNames = Some(Seq("abcdefg")),
      totalAmount = 10000.89
    )

  val json: JsValue = Json.parse(
    """
      |{
      |   "charityNames": [
      |     "abcdefg"
      |   ],
      |   "totalAmount": 10000.89
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        model shouldBe json.as[NonUkCharities]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(model) shouldBe json
      }
    }
  }
}
