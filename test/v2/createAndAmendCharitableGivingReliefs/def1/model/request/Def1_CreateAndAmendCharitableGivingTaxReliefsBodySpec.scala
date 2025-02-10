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

package v2.createAndAmendCharitableGivingReliefs.def1.model.request

import play.api.libs.json.{JsValue, Json}
import shared.utils.UnitSpec

class Def1_CreateAndAmendCharitableGivingTaxReliefsBodySpec extends UnitSpec {

  val nonUkCharitiesModel: Def1_NonUkCharities =
    Def1_NonUkCharities(
      charityNames = Some(Seq("non-UK charity 1", "non-UK charity 2")),
      totalAmount = 1000.12
    )

  val giftAidModel: Def1_GiftAidPayments =
    Def1_GiftAidPayments(
      nonUkCharities = Some(nonUkCharitiesModel),
      totalAmount = Some(1000.12),
      oneOffAmount = Some(1000.12),
      amountTreatedAsPreviousTaxYear = Some(1000.12),
      amountTreatedAsSpecifiedTaxYear = Some(1000.12)
    )

  val giftModel: Def1_Gifts =
    Def1_Gifts(
      nonUkCharities = Some(nonUkCharitiesModel),
      landAndBuildings = Some(1000.12),
      sharesOrSecurities = Some(1000.12)
    )

  val model: Def1_CreateAndAmendCharitableGivingTaxReliefsBody =
    Def1_CreateAndAmendCharitableGivingTaxReliefsBody(
      giftAidPayments = Some(giftAidModel),
      gifts = Some(giftModel)
    )

  val mtdJson: JsValue = Json.parse("""
      |{
      |   "giftAidPayments":{
      |      "nonUkCharities":{
      |         "charityNames":[
      |            "non-UK charity 1",
      |            "non-UK charity 2"
      |         ],
      |         "totalAmount":1000.12
      |      },
      |      "totalAmount":1000.12,
      |      "oneOffAmount":1000.12,
      |      "amountTreatedAsPreviousTaxYear":1000.12,
      |      "amountTreatedAsSpecifiedTaxYear":1000.12
      |   },
      |   "gifts":{
      |      "nonUkCharities":{
      |         "charityNames":[
      |            "non-UK charity 1",
      |            "non-UK charity 2"
      |         ],
      |         "totalAmount":1000.12
      |      },
      |      "landAndBuildings":1000.12,
      |      "sharesOrSecurities":1000.12
      |   }
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse("""
      |{
      |   "giftAidPayments":{
      |      "nonUkCharitiesCharityNames":[
      |         "non-UK charity 1",
      |         "non-UK charity 2"
      |      ],
      |      "nonUkCharities":1000.12,
      |      "currentYear":1000.12,
      |      "oneOffCurrentYear":1000.12,
      |      "currentYearTreatedAsPreviousYear":1000.12,
      |      "nextYearTreatedAsCurrentYear":1000.12
      |   },
      |   "gifts":{
      |      "investmentsNonUkCharitiesCharityNames":[
      |         "non-UK charity 1",
      |         "non-UK charity 2"
      |      ],
      |      "investmentsNonUkCharities":1000.12,
      |      "landAndBuildings":1000.12,
      |      "sharesOrSecurities":1000.12
      |   }
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        mtdJson.as[Def1_CreateAndAmendCharitableGivingTaxReliefsBody] shouldBe model
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
