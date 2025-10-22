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

package v3.fixtures.createAndAmendCharitableGivingTaxReliefs

import play.api.libs.json.{JsValue, Json}
import v3.charitableGiving.createAmend.def2.model.request.{Def2_CreateAndAmendCharitableGivingTaxReliefsBody, Def2_GiftAidPayments, Def2_Gifts}

object Def2_CreateAndAmendCharitableGivingTaxReliefsFixtures {

  val giftAidModel: Def2_GiftAidPayments =
    Def2_GiftAidPayments(
      totalAmount = Some(1000.12),
      oneOffAmount = Some(1000.12),
      amountTreatedAsPreviousTaxYear = Some(1000.12),
      amountTreatedAsSpecifiedTaxYear = Some(1000.12)
    )

  val giftsModel: Def2_Gifts =
    Def2_Gifts(
      landAndBuildings = Some(1000.12),
      sharesOrSecurities = Some(1000.12)
    )

  val model: Def2_CreateAndAmendCharitableGivingTaxReliefsBody =
    Def2_CreateAndAmendCharitableGivingTaxReliefsBody(
      giftAidPayments = Some(giftAidModel),
      gifts = Some(giftsModel)
    )

  val mtdJson: JsValue = Json.parse("""
    |{
    |   "giftAidPayments":{
    |      "totalAmount":1000.12,
    |      "oneOffAmount":1000.12,
    |      "amountTreatedAsPreviousTaxYear":1000.12,
    |      "amountTreatedAsSpecifiedTaxYear":1000.12
    |   },
    |   "gifts":{
    |      "landAndBuildings":1000.12,
    |      "sharesOrSecurities":1000.12
    |   }
    |}
    |""".stripMargin)

  val giftAidMtdJson: JsValue = Json.parse(
    """
      |{
      |     "totalAmount":1000.12,
      |     "oneOffAmount":1000.12,
      |     "amountTreatedAsPreviousTaxYear":1000.12,
      |     "amountTreatedAsSpecifiedTaxYear":1000.12
      |}
      |""".stripMargin
  )

  val giftsMtdJson: JsValue = Json.parse("""
      |{
      |      "landAndBuildings":1000.12,
      |      "sharesOrSecurities":1000.12
      |}
      |""".stripMargin)

  val desJson: JsValue = Json.parse("""
      |{
      |   "giftAidPayments":{
      |      "currentYear":1000.12,
      |      "oneOffCurrentYear":1000.12,
      |      "currentYearTreatedAsPreviousYear":1000.12,
      |      "nextYearTreatedAsCurrentYear":1000.12
      |   },
      |   "gifts":{
      |      "landAndBuildings":1000.12,
      |      "sharesOrSecurities":1000.12
      |   }
      |}
      |""".stripMargin)

  val giftAidDesJson: JsValue = Json.parse(
    """
      |{
      |    "currentYear":1000.12,
      |    "oneOffCurrentYear":1000.12,
      |    "currentYearTreatedAsPreviousYear":1000.12,
      |    "nextYearTreatedAsCurrentYear":1000.12
      |}
      |""".stripMargin
  )

  val giftsDesJson: JsValue = Json.parse(
    """
      |{
      |     "landAndBuildings":1000.12,
      |     "sharesOrSecurities":1000.12
      |}
      |""".stripMargin
  )

}
