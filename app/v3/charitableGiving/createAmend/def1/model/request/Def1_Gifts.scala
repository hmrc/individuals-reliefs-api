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

package v3.charitableGiving.createAmend.def1.model.request

import play.api.libs.json.{JsObject, Json, Reads, Writes}

case class Def1_Gifts(nonUkCharities: Option[Def1_NonUkCharities], landAndBuildings: Option[BigDecimal], sharesOrSecurities: Option[BigDecimal])

object Def1_Gifts {

  implicit val reads: Reads[Def1_Gifts] = Json.reads[Def1_Gifts]

  implicit val writes: Writes[Def1_Gifts] = new Writes[Def1_Gifts] {

    def writes(o: Def1_Gifts): JsObject = Json.obj(
      "investmentsNonUkCharitiesCharityNames" -> o.nonUkCharities.map(_.charityNames),
      "investmentsNonUkCharities"             -> o.nonUkCharities.map(_.totalAmount),
      "landAndBuildings"                      -> o.landAndBuildings,
      "sharesOrSecurities"                    -> o.sharesOrSecurities
    )

  }

}
