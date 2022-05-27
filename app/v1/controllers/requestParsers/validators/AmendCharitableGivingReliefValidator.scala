/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.controllers.requestParsers.validators

import v1.models.errors.MtdError
import v1.models.request.createAndAmendCharitableGivingTaxRelief.CreateAndAmendCharitableGivingTaxReliefRawData

class AmendCharitableGivingReliefValidator extends Validator[CreateAndAmendCharitableGivingTaxReliefRawData] {
  override def validate(data: CreateAndAmendCharitableGivingTaxReliefRawData): List[MtdError] = ???
}
