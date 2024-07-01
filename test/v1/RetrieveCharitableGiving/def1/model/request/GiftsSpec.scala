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

package v1.RetrieveCharitableGiving.def1.model.request

import play.api.libs.json.{JsObject, Json}
import support.UnitSpec
import v1.RetrieveCharitableGiving.def1.model.response.{Def1_Gifts, Def1_NonUkCharities}

class GiftsSpec extends UnitSpec {

  "Gifts reads" must {
    "include the nonUkCharities object in the model" when {
      "a nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                   |   "investmentsNonUkCharitiesCharityNames": ["charity 1", "charity 2"],
                   |   "investmentsNonUkCharities": 1.12,
                   |   "landAndBuildings": 2.12,
                   |   "sharesOrSecurities": 3.12
                   |}""".stripMargin)
          .as[Def1_Gifts] shouldBe
          Def1_Gifts(
            nonUkCharities = Some(Def1_NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
            landAndBuildings = Some(2.12),
            sharesOrSecurities = Some(3.12)
          )
      }
    }

    "omit the nonUkCharities object from the model" when {
      "no nonUkCharities value is present in the downstream JSON" in {
        Json
          .parse("""{
                   |   "landAndBuildings": 2.12,
                   |   "sharesOrSecurities": 3.12
                   |}""".stripMargin)
          .as[Def1_Gifts] shouldBe
          Def1_Gifts(
            nonUkCharities = None,
            landAndBuildings = Some(2.12),
            sharesOrSecurities = Some(3.12)
          )
      }
    }

    "treat all fields as optional" in {
      JsObject.empty.as[Def1_Gifts] shouldBe
        Def1_Gifts(
          nonUkCharities = None,
          landAndBuildings = None,
          sharesOrSecurities = None
        )
    }
  }

  "Gifts writes" must {
    "write to MTD JSON" in {
      Json.toJson(
        Def1_Gifts(
          nonUkCharities = Some(Def1_NonUkCharities(charityNames = Some(Seq("charity 1", "charity 2")), totalAmount = 1.12)),
          landAndBuildings = Some(2.12),
          sharesOrSecurities = Some(3.12)
        )) shouldBe Json.parse("""{
                                       |      "nonUkCharities": {
                                       |         "charityNames": ["charity 1", "charity 2"],
                                       |         "totalAmount": 1.12
                                       |      },
                                       |      "landAndBuildings": 2.12,
                                       |      "sharesOrSecurities": 3.12
                                       |   }""".stripMargin)
    }
  }

}
