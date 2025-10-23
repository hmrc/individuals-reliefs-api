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

package v3.charitableGiving.retrieve.def2.model.request

import play.api.libs.json.*
import shared.utils.UnitSpec
import v3.charitableGiving.retrieve.def2.model.response.Def2_Gifts

class GiftsSpec extends UnitSpec {

  "Gifts reads" must {
    "read from valid JSON and return the expected object" in {
      Json
        .parse("""{
                   |   "landAndBuildings": 2.12,
                   |   "sharesOrSecurities": 3.12
                   |}""".stripMargin)
        .as[Def2_Gifts] shouldBe
        Def2_Gifts(
          landAndBuildings = Some(2.12),
          sharesOrSecurities = Some(3.12)
        )
    }

    "treat all fields as optional" in {
      JsObject.empty.as[Def2_Gifts] shouldBe
        Def2_Gifts(
          landAndBuildings = None,
          sharesOrSecurities = None
        )
    }
  }

  "Gifts writes" must {
    "write to MTD JSON" in {
      Json.toJson(
        Def2_Gifts(
          landAndBuildings = Some(2.12),
          sharesOrSecurities = Some(3.12)
        )) shouldBe Json.parse("""{
                                       |      "landAndBuildings": 2.12,
                                       |      "sharesOrSecurities": 3.12
                                       |   }""".stripMargin)
    }
  }

}
