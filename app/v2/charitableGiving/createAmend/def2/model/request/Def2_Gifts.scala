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

package v2.charitableGiving.createAmend.def2.model.request

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Def2_Gifts(landAndBuildings: Option[BigDecimal], sharesOrSecurities: Option[BigDecimal])

object Def2_Gifts {

  implicit val reads: Reads[Def2_Gifts] = Json.reads[Def2_Gifts]

  implicit val writes: OWrites[Def2_Gifts] = (
    (JsPath \ "landAndBuildings").writeNullable[BigDecimal] and
      (JsPath \ "sharesOrSecurities").writeNullable[BigDecimal]
  )(gifts =>
    (
      gifts.landAndBuildings,
      gifts.sharesOrSecurities
    ))

}
