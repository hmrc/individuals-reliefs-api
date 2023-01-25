/*
 * Copyright 2023 HM Revenue & Customs
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

package api.models.errors

import play.api.http.Status._

object NinoFormatError
    extends MtdError(
      code = "FORMAT_NINO",
      message = "The provided NINO is invalid",
      httpStatus = BAD_REQUEST
    )

object TaxYearFormatError
    extends MtdError(
      code = "FORMAT_TAX_YEAR",
      message = "The provided tax year is invalid",
      httpStatus = BAD_REQUEST
    )

object ValueFormatError
    extends MtdError(
      code = "FORMAT_VALUE",
      message = "The field should be between 0 and 99999999999.99",
      httpStatus = BAD_REQUEST
    )

object StringFormatError extends MtdError(code = "FORMAT_STRING", message = "The supplied string format is not valid", httpStatus = BAD_REQUEST)

object DateFormatError
    extends MtdError(
      code = "FORMAT_DATE",
      message = "The field should be in the format YYYY-MM-DD",
      httpStatus = BAD_REQUEST
    )

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

object CountryCodeFormatError
    extends MtdError(
      code = "FORMAT_COUNTRY_CODE",
      message = "The format of the country code is invalid",
      httpStatus = BAD_REQUEST
    )

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError(
      code = "RULE_TAX_YEAR_NOT_SUPPORTED",
      message = "The tax year specified does not lie within the supported range",
      httpStatus = BAD_REQUEST
    )

object RuleIncorrectOrEmptyBodyError
    extends MtdError(
      code = "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED",
      message = "An empty or non-matching body was submitted",
      httpStatus = BAD_REQUEST
    )

object RuleTaxYearRangeInvalidError
    extends MtdError(
      code = "RULE_TAX_YEAR_RANGE_INVALID",
      message = "Tax year range invalid. A tax year range of one year is required",
      httpStatus = BAD_REQUEST
    )

object RuleCountryCodeError
    extends MtdError(
      code = "RULE_COUNTRY_CODE",
      message = "The country code is not a valid ISO 3166-1 alpha-3 country code",
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

//Standard Errors
object NotFoundError
    extends MtdError(
      code = "MATCHING_RESOURCE_NOT_FOUND",
      message = "Matching resource not found",
      httpStatus = NOT_FOUND
    )

object InternalError
    extends MtdError(
      code = "INTERNAL_SERVER_ERROR",
      message = "An internal server error occurred",
      httpStatus = INTERNAL_SERVER_ERROR
    )

object BadRequestError
    extends MtdError(
      code = "INVALID_REQUEST",
      message = "Invalid request",
      httpStatus = BAD_REQUEST
    )

object BVRError
    extends MtdError(
      code = "BUSINESS_ERROR",
      message = "Business validation error",
      httpStatus = BAD_REQUEST
    )

object ServiceUnavailableError
    extends MtdError(
      code = "SERVICE_UNAVAILABLE",
      message = "Internal server error",
      httpStatus = INTERNAL_SERVER_ERROR
    )

object InvalidHttpMethodError extends MtdError("INVALID_HTTP_METHOD", "Invalid HTTP method", METHOD_NOT_ALLOWED)

//Authorisation Errors
object ClientNotAuthorisedError    extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", FORBIDDEN)
object ClientNotAuthenticatedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised", UNAUTHORIZED)
object InvalidBearerTokenError     extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError
    extends MtdError(
      code = "ACCEPT_HEADER_INVALID",
      message = "The accept header is missing or invalid",
      httpStatus = NOT_ACCEPTABLE
    )

object UnsupportedVersionError
    extends MtdError(
      code = "NOT_FOUND",
      message = "The requested resource could not be found",
      httpStatus = NOT_FOUND
    )

object InvalidBodyTypeError
    extends MtdError(
      code = "INVALID_BODY_TYPE",
      message = "Expecting text/json or application/json body",
      httpStatus = UNSUPPORTED_MEDIA_TYPE
    )
