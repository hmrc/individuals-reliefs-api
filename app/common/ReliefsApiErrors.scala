/*
 * Copyright 2024 HM Revenue & Customs
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

package common

import play.api.http.Status.BAD_REQUEST
import shared.models.errors.MtdError

object DateOfInvestmentFormatError
  extends MtdError(
    code = "FORMAT_DATE_OF_INVESTMENT",
    message = "The format of the investment date is invalid",
    httpStatus = BAD_REQUEST
  )

object NameFormatError
  extends MtdError(
    code = "FORMAT_NAME",
    message = "The format of the name is invalid",
    httpStatus = BAD_REQUEST
  )

object UniqueInvestmentRefFormatError
  extends MtdError(
    code = "FORMAT_UNIQUE_INVESTMENT_REFERENCE",
    message = "The format of unique investment reference is invalid",
    httpStatus = BAD_REQUEST
  )

object CustomerReferenceFormatError
  extends MtdError(
    code = "FORMAT_CUSTOMER_REF",
    message = "The provided customer reference is not valid",
    httpStatus = BAD_REQUEST
  )

object ExSpouseNameFormatError
  extends MtdError(
    code = "FORMAT_NAME_EX_SPOUSE",
    message = "The provided ex spouse name is not valid",
    httpStatus = BAD_REQUEST
  )

object BusinessNameFormatError
  extends MtdError(
    code = "FORMAT_NAME_BUSINESS",
    message = "The provided business name is not valid",
    httpStatus = BAD_REQUEST
  )

object NatureOfTradeFormatError
  extends MtdError(
    code = "FORMAT_NATURE_OF_TRADE",
    message = "The provided nature of trade is not valid",
    httpStatus = BAD_REQUEST
  )

object IncomeSourceFormatError
  extends MtdError(
    code = "FORMAT_INCOME_SOURCE",
    message = "The provided income source is not valid",
    httpStatus = BAD_REQUEST
  )

object LenderNameFormatError
  extends MtdError(
    code = "FORMAT_LENDER_NAME",
    message = "The provided lender name is not valid",
    httpStatus = BAD_REQUEST
  )

object RuleSubmissionFailedError
  extends MtdError(
    code = "RULE_SUBMISSION_FAILED",
    message = "The submission cannot be completed due to validation failures",
    httpStatus = BAD_REQUEST
  )

object RuleGiftAidNonUkAmountWithoutNamesError
  extends MtdError(
    code = "RULE_GIFT_AID_NON_UK_AMOUNT_WITHOUT_NAMES",
    message = "Non-UK charity Gift Aid amount supplied without the non-UK Gift Aid charity names",
    httpStatus = BAD_REQUEST
  )

object RuleGiftsNonUkAmountWithoutNamesError
  extends MtdError(
    code = "RULE_GIFTS_NON_UK_AMOUNT_WITHOUT_NAMES",
    message = "Non-UK gifts amount supplied without non-UK gifts charity names",
    httpStatus = BAD_REQUEST
  )

object RuleOutsideAmendmentWindowError
  extends MtdError(
    code = "RULE_OUTSIDE_AMENDMENT_WINDOW",
    message = "You are outside the amendment window",
    httpStatus = BAD_REQUEST
  )
