/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{Json, OWrites}

case class MtdError(code: String, message: String, paths: Option[Seq[String]] = None)

object MtdError {
  implicit val writes: OWrites[MtdError] = Json.writes[MtdError]

  implicit def genericWrites[T <: MtdError]: OWrites[T] =
    writes.contramap[T](c => c: MtdError)

}

object MtdErrorWithCustomMessage {
  def unapply(arg: MtdError): Option[String] = Some(arg.code)
}

object NinoFormatError
    extends MtdError(
      code = "FORMAT_NINO",
      message = "The provided NINO is invalid"
    )

object TaxYearFormatError
    extends MtdError(
      code = "FORMAT_TAX_YEAR",
      message = "The provided tax year is invalid"
    )

object ValueFormatError
    extends MtdError(
      code = "FORMAT_VALUE",
      message = "The field should be between 0 and 99999999999.99"
    )

object StringFormatError extends MtdError("FORMAT_STRING", "The supplied string format is not valid")

object DateFormatError
    extends MtdError(
      code = "FORMAT_DATE",
      message = "The field should be in the format YYYY-MM-DD"
    )

object DateOfInvestmentFormatError
    extends MtdError(
      code = "FORMAT_DATE_OF_INVESTMENT",
      message = "The format of the investment date is invalid"
    )

object NameFormatError
    extends MtdError(
      code = "FORMAT_NAME",
      message = "The format of the name is invalid"
    )

object UniqueInvestmentRefFormatError
    extends MtdError(
      code = "FORMAT_UNIQUE_INVESTMENT_REFERENCE",
      message = "The format of unique investment reference is invalid"
    )

object CustomerReferenceFormatError
    extends MtdError(
      code = "FORMAT_CUSTOMER_REF",
      message = "The provided customer reference is not valid"
    )

object ExSpouseNameFormatError
    extends MtdError(
      code = "FORMAT_NAME_EX_SPOUSE",
      message = "The provided ex spouse name is not valid"
    )

object BusinessNameFormatError
    extends MtdError(
      code = "FORMAT_NAME_BUSINESS",
      message = "The provided business name is not valid"
    )

object NatureOfTradeFormatError
    extends MtdError(
      code = "FORMAT_NATURE_OF_TRADE",
      message = "The provided nature of trade is not valid"
    )

object IncomeSourceFormatError
    extends MtdError(
      code = "FORMAT_INCOME_SOURCE",
      message = "The provided income source is not valid"
    )

object LenderNameFormatError
    extends MtdError(
      code = "FORMAT_LENDER_NAME",
      message = "The provided lender name is not valid"
    )

object CountryCodeFormatError
    extends MtdError(
      code = "FORMAT_COUNTRY_CODE",
      message = "The format of the country code is invalid"
    )

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError(
      code = "RULE_TAX_YEAR_NOT_SUPPORTED",
      message = "The specified tax year is not supported. That is, the tax year specified is before the minimum tax year value"
    )

object RuleIncorrectOrEmptyBodyError
    extends MtdError(
      code = "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED",
      message = "An empty or non-matching body was submitted"
    )

object RuleTaxYearRangeInvalidError
    extends MtdError(
      code = "RULE_TAX_YEAR_RANGE_INVALID",
      message = "Tax year range invalid. A tax year range of one year is required"
    )

object RuleCountryCodeError
    extends MtdError(
      code = "RULE_COUNTRY_CODE",
      message = "The country code is not a valid ISO 3166-1 alpha-3 country code"
    )

object RuleSubmissionFailedError
    extends MtdError(
      code = "RULE_SUBMISSION_FAILED",
      message = "The submission cannot be completed due to validation failures"
    )

object RuleGiftAidNonUkAmountWithoutNamesError
    extends MtdError(
      code = "RULE_GIFT_AID_NON_UK_AMOUNT_WITHOUT_NAMES",
      message = "Non-UK charity Gift Aid amount supplied without the non-UK Gift Aid charity names"
    )

object RuleGiftsNonUkAmountWithoutNamesError
    extends MtdError(
      code = "RULE_GIFTS_NON_UK_AMOUNT_WITHOUT_NAMES",
      message = "Non-UK gifts amount supplied without non-UK gifts charity names"
    )

//Standard Errors
object NotFoundError
    extends MtdError(
      code = "MATCHING_RESOURCE_NOT_FOUND",
      message = "Matching resource not found"
    )

object InternalError
    extends MtdError(
      code = "INTERNAL_SERVER_ERROR",
      message = "An internal server error occurred"
    )

object BadRequestError
    extends MtdError(
      code = "INVALID_REQUEST",
      message = "Invalid request"
    )

object BVRError
    extends MtdError(
      code = "BUSINESS_ERROR",
      message = "Business validation error"
    )

object ServiceUnavailableError
    extends MtdError(
      code = "SERVICE_UNAVAILABLE",
      message = "Internal server error"
    )

//Authorisation Errors
object UnauthorisedError
    extends MtdError(
      code = "CLIENT_OR_AGENT_NOT_AUTHORISED",
      message = "The client and/or agent is not authorised"
    )

object InvalidBearerTokenError
    extends MtdError(
      code = "UNAUTHORIZED",
      message = "Bearer token is missing or not authorized"
    )

// Accept header Errors
object InvalidAcceptHeaderError
    extends MtdError(
      code = "ACCEPT_HEADER_INVALID",
      message = "The accept header is missing or invalid"
    )

object UnsupportedVersionError
    extends MtdError(
      code = "NOT_FOUND",
      message = "The requested resource could not be found"
    )

object InvalidBodyTypeError
    extends MtdError(
      code = "INVALID_BODY_TYPE",
      message = "Expecting text/json or application/json body"
    )
