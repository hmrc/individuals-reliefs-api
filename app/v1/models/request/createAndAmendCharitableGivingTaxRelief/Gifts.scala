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

import play.api.libs.json.{JsObject, Json, Reads, Writes}

case class Gifts(nonUkCharities: Option[NonUkCharities],
                 landAndBuildings: Option[BigDecimal],
                 sharesOrSecurities: Option[BigDecimal])

object Gifts {

  implicit val reads: Reads[Gifts] = Json.reads[Gifts]

  implicit val writes: Writes[Gifts] = new Writes[Gifts] {
    def writes(o: Gifts): JsObject = Json.obj(
      "investmentsNonUkCharitiesCharityNames" -> o.nonUkCharities.map(_.charityNames),
      "investmentsNonUkCharities" -> o.nonUkCharities.map(_.totalAmount),
      "landAndBuildings" -> o.landAndBuildings,
      "sharesOrSecurities" -> o.sharesOrSecurities,
    )
  }
}
