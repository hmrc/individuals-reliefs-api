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

package v1.createAndAmendCharitableGivingReliefs.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class Def1_GiftAidPaymentsSpec extends UnitSpec {

  val nonUkCharitiesModel: Def1_NonUkCharities =
    Def1_NonUkCharities(
      charityNames = Some(Seq("abcdefghijklmnopqr")),
      totalAmount = 123.89
    )

  val model: Def1_GiftAidPayments =
    Def1_GiftAidPayments(
      nonUkCharities = Some(nonUkCharitiesModel),
      totalAmount = Some(492.11),
      oneOffAmount = Some(987.89),
      amountTreatedAsPreviousTaxYear = Some(4522.20),
      amountTreatedAsSpecifiedTaxYear = Some(10000.89)
    )

  val mtdJson: JsValue = Json.parse("""
      |{
      |   "nonUkCharities": {
      |      "charityNames":[
      |         "abcdefghijklmnopqr"
      |      ],
      |      "totalAmount": 123.89
      |   },
      |   "totalAmount":492.11,
      |   "oneOffAmount":987.89,
      |   "amountTreatedAsPreviousTaxYear":4522.20,
      |   "amountTreatedAsSpecifiedTaxYear": 10000.89
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse("""
      |{
      |   "nonUkCharitiesCharityNames":[
      |      "abcdefghijklmnopqr"
      |   ],
      |   "nonUkCharities":123.89,
      |   "currentYear":492.11,
      |   "oneOffCurrentYear":987.89,
      |   "currentYearTreatedAsPreviousYear":4522.20,
      |   "nextYearTreatedAsCurrentYear":10000.89
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def1_GiftAidPayments] shouldBe model
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
