/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class Gifts(nonUkCharities: Option[NonUkCharities], landAndBuildings: Option[BigDecimal], sharesOrSecurities: Option[BigDecimal])

object Gifts {

  implicit val reads: Reads[Gifts] = {
    val nonUkCharitiesOptionReads =
      ((JsPath \ "investmentsNonUkCharitiesCharityNames").readNullable[Seq[String]] and
        (JsPath \ "investmentsNonUkCharities").readNullable[BigDecimal])(NonUkCharities.from _)

    (nonUkCharitiesOptionReads and
      (JsPath \ "landAndBuildings").readNullable[BigDecimal] and
      (JsPath \ "sharesOrSecurities").readNullable[BigDecimal])(Gifts.apply _)
  }

  implicit val writes: Writes[Gifts] = Json.writes
}
