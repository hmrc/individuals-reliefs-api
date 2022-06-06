/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.models.response.retrieveCharitableGivingTaxRelief

import play.api.libs.json.{Json, OFormat}

case class RetrieveCharitableGivingReliefResponse(giftAidPayments: Option[GiftAidPayments], gifts: Option[Gifts])

object RetrieveCharitableGivingReliefResponse {
  implicit val format: OFormat[RetrieveCharitableGivingReliefResponse] = Json.format
}
