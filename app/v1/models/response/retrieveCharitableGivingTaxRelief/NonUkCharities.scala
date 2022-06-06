/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{Json, Writes}

case class NonUkCharities(charityNames: Option[Seq[String]], totalAmount: BigDecimal)

object NonUkCharities {
  implicit val writes: Writes[NonUkCharities] = Json.writes

  private[retrieveCharitableGivingTaxRelief] def from(charityNames: Option[Seq[String]], totalAmount: Option[BigDecimal]): Option[NonUkCharities] =
    totalAmount.map(NonUkCharities(charityNames, _))

}
