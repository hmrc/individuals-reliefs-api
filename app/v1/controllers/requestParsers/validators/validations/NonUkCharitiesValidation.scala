/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v1.controllers.requestParsers.validators.validations

import v1.models.request.createAndAmendCharitableGivingTaxRelief.NonUkCharities

object NonUkCharitiesValidation {

  private val regex = "^[A-Za-z0-9 &'()*,\\-./@£]{1,75}$".r.pattern

  def hasMissingNames(nonUkCharities: NonUkCharities): Boolean =
    nonUkCharities match {
      case NonUkCharities(_, totalAmount) if totalAmount <= 0 => false
      case NonUkCharities(None, _)                            => true
      case NonUkCharities(Some(cns), _)                       => cns.isEmpty
    }

  def isNameValid(charityName: String): Boolean = regex.matcher(charityName).matches()
}
