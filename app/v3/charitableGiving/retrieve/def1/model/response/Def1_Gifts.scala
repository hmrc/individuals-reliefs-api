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

package v3.charitableGiving.retrieve.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Def1_Gifts(nonUkCharities: Option[Def1_NonUkCharities], landAndBuildings: Option[BigDecimal], sharesOrSecurities: Option[BigDecimal])

object Def1_Gifts {

  implicit val reads: Reads[Def1_Gifts] = {
    val nonUkCharitiesOptionReads =
      ((JsPath \ "investmentsNonUkCharitiesCharityNames").readNullable[Seq[String]] and
        (JsPath \ "investmentsNonUkCharities").readNullable[BigDecimal])(Def1_NonUkCharities.from _)

    (nonUkCharitiesOptionReads and
      (JsPath \ "landAndBuildings").readNullable[BigDecimal] and
      (JsPath \ "sharesOrSecurities").readNullable[BigDecimal])(Def1_Gifts.apply _)
  }

  implicit val writes: Writes[Def1_Gifts] = Json.writes

}
