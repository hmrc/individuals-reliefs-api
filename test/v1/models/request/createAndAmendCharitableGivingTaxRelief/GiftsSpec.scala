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

package v1.models.request.createAndAmendCharitableGivingTaxRelief

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class GiftsSpec extends UnitSpec {

  val nonUkCharitiesModel: NonUkCharities =
    NonUkCharities(
      charityNames = Some(Seq("abcdefghijklmnopqr")),
      totalAmount = 492.10
    )

  val model: Gifts =
    Gifts(
      nonUkCharities = Some(nonUkCharitiesModel),
      landAndBuildings = Some(231.29),
      sharesOrSecurities = Some(10000.89),
    )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "nonUkCharities": {
      |      "charityNames":[
      |         "abcdefghijklmnopqr"
      |      ],
      |      "totalAmount": 492.10
      |   },
      |   "landAndBuildings":231.29,
      |   "sharesOrSecurities":10000.89
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse(
    """
      |{
      |   "investmentsNonUkCharitiesCharityNames":[
      |      "abcdefghijklmnopqr"
      |   ],
      |   "investmentsNonUkCharities":492.10,
      |   "landAndBuildings":231.29,
      |   "sharesOrSecurities":10000.89
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Gifts] shouldBe model
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(model) shouldBe desJson
      }
    }
  }
}
