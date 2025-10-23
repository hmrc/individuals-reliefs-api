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

package v3.charitableGiving.retrieve.def1.model.request

import play.api.libs.json.*
import shared.utils.UnitSpec
import v3.charitableGiving.retrieve.def1.model.response.{Def1_GiftAidPayments, Def1_NonUkCharities}

class GiftAidPaymentsSpec extends UnitSpec {

  "GiftAidPayments reads" must {
    "include the nonUkCharities object in the model" when {
      "a nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                    |   "nonUkCharitiesCharityNames": ["charity 1", "charity 2"],
                    |   "nonUkCharities": 1.12,
                    |   "currentYear": 2.12,
                    |   "oneOffCurrentYear": 3.12,
                    |   "currentYearTreatedAsPreviousYear": 4.12,
                    |   "nextYearTreatedAsCurrentYear": 5.12
                    |}""".stripMargin)
          .as[Def1_GiftAidPayments] shouldBe
          Def1_GiftAidPayments(
            nonUkCharities = Some(Def1_NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
            totalAmount = Some(2.12),
            oneOffAmount = Some(3.12),
            amountTreatedAsPreviousTaxYear = Some(4.12),
            amountTreatedAsSpecifiedTaxYear = Some(5.12)
          )
      }
    }

    "omit the nonUkCharities object from the model" when {
      "no nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                   |   "currentYear": 2.12,
                   |   "oneOffCurrentYear": 3.12,
                   |   "currentYearTreatedAsPreviousYear": 4.12,
                   |   "nextYearTreatedAsCurrentYear": 5.12
                   |}""".stripMargin)
          .as[Def1_GiftAidPayments] shouldBe
          Def1_GiftAidPayments(
            nonUkCharities = None,
            totalAmount = Some(2.12),
            oneOffAmount = Some(3.12),
            amountTreatedAsPreviousTaxYear = Some(4.12),
            amountTreatedAsSpecifiedTaxYear = Some(5.12)
          )
      }

      "omit charityNames when not present" in {
        Json
          .parse("""{
              |  "totalAmount": 1.12
              |}""".stripMargin)
          .as[Def1_NonUkCharities] shouldBe
          Def1_NonUkCharities(
            charityNames = None,
            totalAmount = 1.12
          )
      }
    }

    "treat all fields as optional" in {
      JsObject.empty.as[Def1_GiftAidPayments] shouldBe
        Def1_GiftAidPayments(
          nonUkCharities = None,
          totalAmount = None,
          oneOffAmount = None,
          amountTreatedAsPreviousTaxYear = None,
          amountTreatedAsSpecifiedTaxYear = None
        )
    }
  }

  "GiftAidPayments writes" must {
    "write to MTD JSON" in {
      Json.toJson(
        Def1_GiftAidPayments(
          nonUkCharities = Some(Def1_NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
          totalAmount = Some(2.12),
          oneOffAmount = Some(3.12),
          amountTreatedAsPreviousTaxYear = Some(4.12),
          amountTreatedAsSpecifiedTaxYear = Some(5.12)
        )) shouldBe Json.parse("""{
                                 |   "nonUkCharities": {
                                 |      "charityNames": ["charity 1", "charity 2"],
                                 |      "totalAmount": 1.12
                                 |   },
                                 |   "totalAmount": 2.12,
                                 |   "oneOffAmount": 3.12,
                                 |   "amountTreatedAsPreviousTaxYear": 4.12,
                                 |   "amountTreatedAsSpecifiedTaxYear": 5.12
                                 |}""".stripMargin)
    }
  }

  "error when JSON is invalid" in {
    val invalidJson = Json.obj(
      "nonUkCharities" -> Json.arr(1, 2, 3)
    )
    invalidJson.validate[Def1_GiftAidPayments] shouldBe a[JsError]
  }

}
