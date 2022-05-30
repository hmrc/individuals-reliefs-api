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
import v1.models.request.createAndAmendCharitableGivingTaxRelief.{NonUkCharities, GiftAidPayments}

class GiftAidPaymentsSpec extends UnitSpec {

  val nonUkCharitiesModel: NonUkCharities =
    NonUkCharities(
      charityNames = Some(Seq("abcdefghijklmnopqr")),
      totalAmount = 10000.89
    )

  val model: GiftAidPayments =
    GiftAidPayments(
      nonUkCharities = Some(nonUkCharitiesModel),
      totalAmount = Some(10000.89),
      oneOffAmount = Some(10000.89),
      amountTreatedAsPreviousTaxYear = Some(10000.89),
      amountTreatedAsSpecifiedTaxYear = Some(10000.89)
    )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "nonUkCharities": {
      |      "charityNames":[
      |         "abcdefghijklmnopqr"
      |      ],
      |      "totalAmount": 10000.89
      |   },
      |   "totalAmount":10000.89,
      |   "oneOffAmount":10000.89,
      |   "amountTreatedAsPreviousTaxYear":10000.89,
      |   "amountTreatedAsSpecifiedTaxYear": 10000.89
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse(
    """
      |{
      |   "nonUkCharitiesCharityNames":[
      |      "abcdefghijklmnopqr"
      |   ],
      |   "nonUkCharities":10000.89,
      |   "currentYear":10000.89,
      |   "oneOffCurrentYear":10000.89,
      |   "currentYearTreatedAsPreviousYear":10000.89,
      |   "nextYearTreatedAsCurrentYear":10000.89
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        model shouldBe mtdJson.as[GiftAidPayments]
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
