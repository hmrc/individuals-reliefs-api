/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.errors

import play.api.libs.json.{Json, Writes}

case class MtdError(code: String, message: String, paths: Option[Seq[String]] = None)

object MtdError {
  implicit val writes: Writes[MtdError] = Json.writes[MtdError]
}

object MtdErrorWithCustomMessage {
  def unapply(arg: MtdError): Option[String] = Some(arg.code)
}

object NinoFormatError extends MtdError("FORMAT_NINO", "The provided NINO is invalid")

object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The provided tax year is invalid")

object ValueFormatError extends MtdError("FORMAT_VALUE", "The field should be between 0 and 99999999999.99")

object DateFormatError extends MtdError("FORMAT_DATE", "The field should be in the format YYYY-MM-DD")

object DateOfInvestmentFormatError extends MtdError("FORMAT_DATE_OF_INVESTMENT", "The format of the investment date is invalid")

object NameFormatError extends MtdError("FORMAT_NAME", "The format of the name is invalid")

object UniqueInvestmentRefFormatError extends MtdError("FORMAT_UNIQUE_INVESTMENT_REFERENCE", "The format of unique investment reference is invalid")

object CustomerReferenceFormatError extends MtdError("FORMAT_CUSTOMER_REF", "The provided customer reference is not valid")

object ExSpouseNameFormatError extends MtdError("FORMAT_NAME_EX_SPOUSE", "The provided ex spouse name is not valid")

object BusinessNameFormatError extends MtdError("FORMAT_NAME_BUSINESS", "The provided business name is not valid")

object NatureOfTradeFormatError extends MtdError("FORMAT_NATURE_OF_TRADE", "The provided nature of trade is not valid")

object IncomeSourceFormatError extends MtdError("FORMAT_INCOME_SOURCE", "The provided income source is not valid")

object LenderNameFormatError extends MtdError("FORMAT_LENDER_NAME", "The provided lender name is not valid")

object CountryCodeFormatError extends MtdError("FORMAT_COUNTRY_CODE", "The format of the country code is invalid")


// Rule Errors
object RuleTaxYearNotSupportedError extends MtdError(
  "RULE_TAX_YEAR_NOT_SUPPORTED",
  "The specified tax year is not supported. That is, the tax year specified is before the minimum tax year value"
)

object RuleIncorrectOrEmptyBodyError extends MtdError("RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED", "An empty or non-matching body was submitted")

object RuleTaxYearRangeInvalidError
  extends MtdError("RULE_TAX_YEAR_RANGE_INVALID", "Tax year range invalid. A tax year range of one year is required")

object RuleCountryCodeError extends MtdError("RULE_COUNTRY_CODE", "The country code is not a valid ISO 3166-1 alpha-3 country code")

//Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")

object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

//Authorisation Errors
object UnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")

object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Accept header Errors
object InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")

