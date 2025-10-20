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

package v1.retrieveCharitableGivingReliefs.def2.model.request

import play.api.libs.json.{JsValue, Json}
import v1.retrieveCharitableGivingReliefs.def2.model.response.{Def2_GiftAidPayments, Def2_Gifts}
import v1.retrieveCharitableGivingReliefs.model.response.Def2_RetrieveCharitableGivingReliefsResponse

trait RetrieveCharitableGivingReliefsFixture {

  val def2_charitableGivingReliefsResponseMtdJson: JsValue = Json.parse("""
                                                                          |{
                                                                          |   "giftAidPayments": {
                                                                          |     "totalAmount": 2.12,
                                                                          |      "oneOffAmount": 3.12,
                                                                          |      "amountTreatedAsPreviousTaxYear": 4.12,
                                                                          |      "amountTreatedAsSpecifiedTaxYear": 5.12
                                                                          |   },
                                                                          |   "gifts": {
                                                                          |      "landAndBuildings": 7.12,
                                                                          |      "sharesOrSecurities": 8.12
                                                                          |   }
                                                                          |}
                                                                          |""".stripMargin)

  val charitableGivingReliefsResponse: Def2_RetrieveCharitableGivingReliefsResponse =
    Def2_RetrieveCharitableGivingReliefsResponse(
      giftAidPayments = Some(
        Def2_GiftAidPayments(
          totalAmount = Some(2.12),
          oneOffAmount = Some(3.12),
          amountTreatedAsPreviousTaxYear = Some(4.12),
          amountTreatedAsSpecifiedTaxYear = Some(5.12)
        )),
      gifts = Some(
        Def2_Gifts(
          landAndBuildings = Some(7.12),
          sharesOrSecurities = Some(8.12)
        ))
    )

  val charitableGivingReliefsIfsResponseDownstreamJson: JsValue = Json.parse("""
                                                                               |{
                                                                               |   "charitableGivingAnnual" : {
                                                                               |     "giftAidPayments": {
                                                                               |        "currentYear": 2.12,
                                                                               |        "oneOffCurrentYear": 3.12,
                                                                               |        "currentYearTreatedAsPreviousYear": 4.12,
                                                                               |        "nextYearTreatedAsCurrentYear": 5.12
                                                                               |     },
                                                                               |     "gifts": {
                                                                               |        "landAndBuildings": 7.12,
                                                                               |        "sharesOrSecurities": 8.12
                                                                               |     }
                                                                               |   }
                                                                               |}
                                                                               |""".stripMargin)

}
