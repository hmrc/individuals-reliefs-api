/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.http.Status.*

// Format Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The NINO format is invalid", BAD_REQUEST)

object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The taxYear format is invalid", BAD_REQUEST)

object DateFormatError extends MtdError(code = "FORMAT_DATE", message = "The supplied date format is not valid", BAD_REQUEST)

object CountryCodeFormatError extends MtdError("FORMAT_COUNTRY_CODE", "The provided Country code is invalid", BAD_REQUEST)

object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0 and 99999999999.99", BAD_REQUEST) {

  def forPathAndRange(path: String, min: String, max: String): MtdError =
    ValueFormatError.copy(paths = Some(Seq(path)), message = s"The value must be between $min and $max")

}

object StringFormatError extends MtdError(code = "FORMAT_STRING", message = "The supplied string format is not valid", BAD_REQUEST)

object DateOfInvestmentFormatError extends MtdError("FORMAT_DATE_OF_INVESTMENT", "The format of the investment date is invalid", BAD_REQUEST)

object NameFormatError extends MtdError("FORMAT_NAME", "The format of the name is invalid", BAD_REQUEST)

object UniqueInvestmentRefFormatError
    extends MtdError("FORMAT_UNIQUE_INVESTMENT_REFERENCE", "The format of unique investment reference is invalid", BAD_REQUEST)

object CustomerReferenceFormatError extends MtdError("FORMAT_CUSTOMER_REF", "The provided customer reference is not valid", BAD_REQUEST)

object ExSpouseNameFormatError extends MtdError("FORMAT_NAME_EX_SPOUSE", "The provided ex spouse name is not valid", BAD_REQUEST)

object BusinessNameFormatError extends MtdError("FORMAT_NAME_BUSINESS", "The provided business name is not valid", BAD_REQUEST)

object NatureOfTradeFormatError extends MtdError("FORMAT_NATURE_OF_TRADE", "The provided nature of trade is not valid", BAD_REQUEST)

object IncomeSourceFormatError extends MtdError("FORMAT_INCOME_SOURCE", "The provided income source is not valid", BAD_REQUEST)

object LenderNameFormatError extends MtdError("FORMAT_LENDER_NAME", "The provided lender name is not valid", BAD_REQUEST)

//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found", NOT_FOUND)

object InternalError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred", INTERNAL_SERVER_ERROR)

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request", BAD_REQUEST)

object GatewayTimeoutError extends MtdError("GATEWAY_TIMEOUT", "The request has timed out", GATEWAY_TIMEOUT)

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error", BAD_REQUEST)

object InvalidHttpMethodError extends MtdError("INVALID_HTTP_METHOD", "Invalid HTTP method", METHOD_NOT_ALLOWED)

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)

//Authentication/Authorisation errors

/** Authentication OK but not allowed access to the requested resource
  */
object ClientOrAgentNotAuthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client or agent is not authorised", FORBIDDEN) {
  def withStatus401: MtdError = copy(httpStatus = UNAUTHORIZED)
}

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized", UNAUTHORIZED)

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid", NOT_ACCEPTABLE)

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found", NOT_FOUND)

// Rule errors
object RuleTaxYearNotSupportedError
    extends MtdError("RULE_TAX_YEAR_NOT_SUPPORTED", "The tax year specified does not lie within the supported range", BAD_REQUEST)

object RuleTaxYearForVersionNotSupportedError
    extends MtdError(
      "RULE_TAX_YEAR_FOR_VERSION_NOT_SUPPORTED",
      "This version of the API supports tax years up to, and including, 2024-25. For tax years from 2025-26 onwards, use version 3",
      BAD_REQUEST)

object RuleIncorrectOrEmptyBodyError
    extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted", BAD_REQUEST)

object RuleTaxYearRangeInvalidError extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "A tax year range of one year is required", BAD_REQUEST)

object RuleTaxYearNotEndedError extends MtdError("RULE_TAX_YEAR_NOT_ENDED", "The specified tax year has not yet ended", BAD_REQUEST)

object RuleCountryCodeError extends MtdError("RULE_COUNTRY_CODE", "The country code is not a permitted ISO 3166-1 alpha-3 country code", BAD_REQUEST)

object RuleSubmissionFailedError
    extends MtdError("RULE_SUBMISSION_FAILED", "The submission cannot be completed due to validation failures", BAD_REQUEST)

object RuleGiftAidNonUkAmountWithoutNamesError
    extends MtdError(
      "RULE_GIFT_AID_NON_UK_AMOUNT_WITHOUT_NAMES",
      "Non-UK charity Gift Aid amount supplied without the non-UK Gift Aid charity names",
      BAD_REQUEST)

object RuleGiftsNonUkAmountWithoutNamesError
    extends MtdError("RULE_GIFTS_NON_UK_AMOUNT_WITHOUT_NAMES", "Non-UK gifts amount supplied without non-UK gifts charity names", BAD_REQUEST)

object RuleOutsideAmendmentWindowError extends MtdError("RULE_OUTSIDE_AMENDMENT_WINDOW", "You are outside the amendment window", BAD_REQUEST)

//Stub Errors
object RuleIncorrectGovTestScenarioError
    extends MtdError("RULE_INCORRECT_GOV_TEST_SCENARIO", "The supplied Gov-Test-Scenario is not valid", BAD_REQUEST)
