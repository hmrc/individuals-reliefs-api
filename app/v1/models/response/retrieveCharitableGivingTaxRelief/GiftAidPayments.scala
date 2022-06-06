/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.libs.functional.syntax._

case class GiftAidPayments(nonUkCharities: Option[NonUkCharities],
                           totalAmount: Option[BigDecimal],
                           oneOffAmount: Option[BigDecimal],
                           amountTreatedAsPreviousTaxYear: Option[BigDecimal],
                           amountTreatedAsSpecifiedTaxYear: Option[BigDecimal])

object GiftAidPayments {

  implicit val reads: Reads[GiftAidPayments] = {
    val nonUkCharitiesOptionReads =
      ((JsPath \ "nonUkCharitiesCharityNames").readNullable[Seq[String]] and
        (JsPath \ "nonUkCharities").readNullable[BigDecimal])(NonUkCharities.from _)

    (nonUkCharitiesOptionReads and
      (JsPath \ "currentYear").readNullable[BigDecimal] and
      (JsPath \ "oneOffCurrentYear").readNullable[BigDecimal] and
      (JsPath \ "currentYearTreatedAsPreviousYear").readNullable[BigDecimal] and
      (JsPath \ "nextYearTreatedAsCurrentYear").readNullable[BigDecimal])(GiftAidPayments.apply _)
  }

  implicit val writes: Writes[GiftAidPayments] = Json.writes
}
