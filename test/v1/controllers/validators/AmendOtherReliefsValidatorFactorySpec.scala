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

package v1.controllers.validators

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.utils.JsonErrorValidators
import mocks.MockAppConfig
import play.api.libs.json._
import support.UnitSpec
import v1.models.request.amendOtherReliefs._

class AmendOtherReliefsValidatorFactorySpec extends UnitSpec with MockAppConfig with JsonErrorValidators {

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2020-21"

  private def bodyWith(maintenancePaymentsEntries: JsValue*)(postCessationEntries: JsValue*)(qualifyingEntries: JsValue*) = Json.parse(
    s"""
      |{
      |  "nonDeductibleLoanInterest": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 763.00
      |  },
      |  "payrollGiving": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 154.00
      |  },
      |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
      |    "customerReference": "myref",
      |    "amount": 222.22
      |  },
      |  "maintenancePayments": ${JsArray(maintenancePaymentsEntries)},
      |  "postCessationTradeReliefAndCertainOtherLosses": ${JsArray(postCessationEntries)},
      |  "annualPaymentsMade": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 763.00
      |  },
      |  "qualifyingLoanInterestPayments": ${JsArray(qualifyingEntries)}
      |}
      |""".stripMargin
  )

  private val validMaintenancePaymentsEntry = Json.parse("""
      |    {
      |      "customerReference": "myref",
      |      "exSpouseName" : "Hilda",
      |      "exSpouseDateOfBirth": "2000-01-01",
      |      "amount": 222.22
      |    }
      |""".stripMargin)

  private val validPostCessationEntry = Json.parse("""
      |    {
      |      "customerReference": "myref",
      |      "businessName": "ACME Inc",
      |      "dateBusinessCeased": "2019-08-10",
      |      "natureOfTrade": "Widgets Manufacturer",
      |      "incomeSource": "AB12412/A12",
      |      "amount": 222.22
      |    }
      |""".stripMargin)

  private val validQualifyingEntry = Json.parse("""
      |    {
      |      "customerReference": "myref",
      |      "lenderName": "Maurice",
      |      "reliefClaimed": 763.00
      |    }
      |""".stripMargin)

  private val validBody = bodyWith(validMaintenancePaymentsEntry)(validPostCessationEntry)(validQualifyingEntry)

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val emptyJson = Json.parse(
    """
      |{}
      |""".stripMargin
  )

  private val parsedNonDeductibleLoanInterest = NonDeductibleLoanInterest(
    customerReference = Some("myref"),
    reliefClaimed = 763.00
  )

  private val parsedPayrollGiving = PayrollGiving(
    customerReference = Some("myref"),
    reliefClaimed = 154.00
  )

  private val parsedQualifyingDistributionRedemptionOfSharesAndSecurities = QualifyingDistributionRedemptionOfSharesAndSecurities(
    customerReference = Some("myref"),
    amount = 222.22
  )

  private val parsedMaintenancePayments = MaintenancePayments(
    customerReference = Some("myref"),
    exSpouseName = Some("Hilda"),
    exSpouseDateOfBirth = Some("2000-01-01"),
    amount = 222.22
  )

  private val parsedPostCessationTradeReliefAndCertainOtherLosses = PostCessationTradeReliefAndCertainOtherLosses(
    customerReference = Some("myref"),
    businessName = Some("ACME Inc"),
    dateBusinessCeased = Some("2019-08-10"),
    natureOfTrade = Some("Widgets Manufacturer"),
    incomeSource = Some("AB12412/A12"),
    amount = 222.22
  )

  private val parsedAnnualPaymentsMade = AnnualPaymentsMade(
    customerReference = Some("myref"),
    reliefClaimed = 763.00
  )

  private val parsedQualifyingLoanInterestPayments = QualifyingLoanInterestPayments(
    customerReference = Some("myref"),
    lenderName = Some("Maurice"),
    reliefClaimed = 763.00
  )

  private val parsedBody = AmendOtherReliefsRequestBody(
    Some(parsedNonDeductibleLoanInterest),
    payrollGiving = Some(parsedPayrollGiving),
    qualifyingDistributionRedemptionOfSharesAndSecurities = Some(parsedQualifyingDistributionRedemptionOfSharesAndSecurities),
    maintenancePayments = Some(Seq(parsedMaintenancePayments)),
    postCessationTradeReliefAndCertainOtherLosses = Some(Seq(parsedPostCessationTradeReliefAndCertainOtherLosses)),
    annualPaymentsMade = Some(parsedAnnualPaymentsMade),
    qualifyingLoanInterestPayments = Some(Seq(parsedQualifyingLoanInterestPayments))
  )

  private val validatorFactory = new AmendOtherReliefsValidatorFactory(mockAppConfig)

  private def validator(nino: String, taxYear: String, body: JsValue) = validatorFactory.validator(nino, taxYear, body)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Right(AmendOtherReliefsRequestData(parsedNino, parsedTaxYear, parsedBody))
      }
    }
    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator("invalid", validTaxYear, validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, "201831", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }
    }
    "return RuleTaxYearRangeInvalidError" when {
      "the tax year range exceeds 1" in {
        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, "2021-24", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }
    "return RuleTaxYearNotSupportedError" when {
      "the given tax year is before the minimum tax year" in {
        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, "2019-20", validBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }
    }
    "return multiple errors" when {
      "request supplied has multiple errors" in {
        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator("invalid", "invalid", validBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError))
          )
        )
      }
    }
    "return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
      "an empty JSON body is submitted" in {
        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, emptyJson).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError))

      }
      "at least one empty array is provided" in {
        val invalidBody = validBody.update("/maintenancePayments", JsArray(List.empty))

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/maintenancePayments")))
      }
      "at least one array contains an empty object" in {
        val invalidBody = validBody.update("/maintenancePayments", JsArray(List(JsObject.empty)))

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/maintenancePayments/0/amount")))
      }
    }
    "return a FORMAT_CUSTOMER_REF error" when {
      "the customerReference provided is invalid" in {
        val invalidCustomerReference = JsString(s"${("1234567890" * 9) + "1"}")

        val invalidMaintenancePaymentsEntry = validMaintenancePaymentsEntry.update("/customerReference", invalidCustomerReference)
        val invalidPostCessationEntry       = validPostCessationEntry.update("/customerReference", invalidCustomerReference)
        val invalidQualifyingEntry          = validQualifyingEntry.update("/customerReference", invalidCustomerReference)

        val invalidBody = bodyWith(invalidMaintenancePaymentsEntry)(invalidPostCessationEntry)(invalidQualifyingEntry)
          .update("/nonDeductibleLoanInterest/customerReference", invalidCustomerReference)
          .update("/payrollGiving/customerReference", invalidCustomerReference)
          .update("/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference", invalidCustomerReference)
          .update("/annualPaymentsMade/customerReference", invalidCustomerReference)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            CustomerReferenceFormatError.withPaths(List(
              "/nonDeductibleLoanInterest/customerReference",
              "/payrollGiving/customerReference",
              "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
              "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
              "/maintenancePayments/0/customerReference",
              "/annualPaymentsMade/customerReference",
              "/qualifyingLoanInterestPayments/0/customerReference"
            ))
          ))

      }
    }
    "return a FORMAT_NAME_EX_SPOUSE error" when {
      "the exSpouseName provided is invalid" in {

        val invalidMaintenancePaymentsEntry = validMaintenancePaymentsEntry.update("/exSpouseName", JsString(""))

        val invalidBody = bodyWith(invalidMaintenancePaymentsEntry)(validPostCessationEntry)(validQualifyingEntry)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ExSpouseNameFormatError.withPath("/maintenancePayments/0/exSpouseName")))

      }
    }
    "return a FORMAT_NAME_BUSINESS error" when {
      "the businessName provided is invalid" in {

        val invalidPostCessationEntry = validPostCessationEntry.update("/businessName", JsString(""))

        val invalidBody = bodyWith(validMaintenancePaymentsEntry)(invalidPostCessationEntry)(validQualifyingEntry)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BusinessNameFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/businessName")))
      }
    }

    "return a FORMAT_NATURE_OF_TRADE error" when {
      "the natureOfTrade provided is invalid" in {
        val invalidPostCessationEntry = validPostCessationEntry.update("/natureOfTrade", JsString(""))

        println(invalidPostCessationEntry)
        val invalidBody = bodyWith(validMaintenancePaymentsEntry)(invalidPostCessationEntry)(validQualifyingEntry)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NatureOfTradeFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade")))
      }
    }
    "return a FORMAT_INCOME_SOURCE error" when {
      "the incomeSource provided is invalid" in {
        val invalidPostCessationEntry = validPostCessationEntry.update("/incomeSource", JsString(""))

        val invalidBody = bodyWith(validMaintenancePaymentsEntry)(invalidPostCessationEntry)(validQualifyingEntry)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, IncomeSourceFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource")))
      }
    }
    "return a FORMAT_LENDER_NAME error" when {
      "the lenderName provided is invalid" in {
        val invalidQualifyingEntry = validQualifyingEntry.update("/lenderName", JsString(""))

        val invalidBody = bodyWith(validMaintenancePaymentsEntry)(validPostCessationEntry)(invalidQualifyingEntry)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, LenderNameFormatError.withPath("/qualifyingLoanInterestPayments/0/lenderName")))
      }
    }
    "return a FORMAT_VALUE error" when {
      "all fields are below 0" in {
        val invalidValue = JsNumber(-1.00)

        val invalidMaintenancePaymentsEntry = validMaintenancePaymentsEntry.update("/amount", invalidValue)
        val invalidPostCessationEntry       = validPostCessationEntry.update("/amount", invalidValue)
        val invalidQualifyingEntry          = validQualifyingEntry.update("/reliefClaimed", invalidValue)

        val invalidBody =
          bodyWith(invalidMaintenancePaymentsEntry, invalidMaintenancePaymentsEntry)(invalidPostCessationEntry)(invalidQualifyingEntry)
            .update("/nonDeductibleLoanInterest/reliefClaimed", invalidValue)
            .update("/payrollGiving/reliefClaimed", invalidValue)
            .update("/qualifyingDistributionRedemptionOfSharesAndSecurities/amount", invalidValue)
            .update("/annualPaymentsMade/reliefClaimed", invalidValue)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(List(
              "/nonDeductibleLoanInterest/reliefClaimed",
              "/payrollGiving/reliefClaimed",
              "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
              "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
              "/maintenancePayments/0/amount",
              "/maintenancePayments/1/amount",
              "/annualPaymentsMade/reliefClaimed",
              "/qualifyingLoanInterestPayments/0/reliefClaimed"
            ))
          ))
      }
      "only some fields are below 0" in {
        val invalidValue = JsNumber(-1.00)

        val invalidMaintenancePaymentsEntry = validMaintenancePaymentsEntry.update("/amount", invalidValue)
        val invalidPostCessationEntry       = validPostCessationEntry.update("/amount", invalidValue)

        val invalidBody =
          bodyWith(invalidMaintenancePaymentsEntry)(invalidPostCessationEntry)(validQualifyingEntry)
            .update("/qualifyingDistributionRedemptionOfSharesAndSecurities/amount", invalidValue)
            .update("/annualPaymentsMade/reliefClaimed", invalidValue)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            ValueFormatError.withPaths(Seq(
              "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
              "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
              "/maintenancePayments/0/amount",
              "/annualPaymentsMade/reliefClaimed"
            ))
          ))
      }
    }
    "return a FORMAT_DATE error" when {
      "the dates are invalid" in {
        val invalidDate = JsString("01-01-230")

        val invalidMaintenancePaymentsEntry = validMaintenancePaymentsEntry.update("/exSpouseDateOfBirth", invalidDate)
        val invalidPostCessationEntry       = validPostCessationEntry.update("/dateBusinessCeased", invalidDate)

        val invalidBody =
          bodyWith(invalidMaintenancePaymentsEntry)(invalidPostCessationEntry)(validQualifyingEntry)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            DateFormatError.withPaths(
              List(
                "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased",
                "/maintenancePayments/0/exSpouseDateOfBirth"
              ))
          ))
      }
    }
    "return all types of errors" when {
      "the provided data violates all errors" in {

        val invalidCustomerReference = JsString(s"""${("1234567890" * 9) + "1"}""")
        val invalidValue             = JsNumber(-1.00)
        val invalidDate              = JsString("01-01-230")

        val invalidMaintenancePaymentsEntry = validMaintenancePaymentsEntry
          .update("/customerReference", invalidCustomerReference)
          .update("/exSpouseName", JsString(""))
          .update("/exSpouseDateOfBirth", invalidDate)
          .update("/amount", invalidValue)

        val invalidPostCessationEntry = validPostCessationEntry
          .update("/customerReference", invalidCustomerReference)
          .update("/businessName", JsString(""))
          .update("/dateBusinessCeased", invalidDate)
          .update("/natureOfTrade", JsString(""))
          .update("/incomeSource", JsString(""))
          .update("/amount", invalidValue)

        val invalidQualifyingEntry = validQualifyingEntry
          .update("/customerReference", invalidCustomerReference)
          .update("/lenderName", JsString(""))
          .update("/reliefClaimed", invalidValue)

        val invalidBody =
          bodyWith(invalidMaintenancePaymentsEntry)(invalidPostCessationEntry)(invalidQualifyingEntry)
            .update("/nonDeductibleLoanInterest/customerReference", invalidCustomerReference)
            .update("/payrollGiving/customerReference", invalidCustomerReference)
            .update("/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference", invalidCustomerReference)
            .update("/annualPaymentsMade/customerReference", invalidCustomerReference)
            .update("/nonDeductibleLoanInterest/reliefClaimed", invalidValue)
            .update("/payrollGiving/reliefClaimed", invalidValue)
            .update("/qualifyingDistributionRedemptionOfSharesAndSecurities/amount", invalidValue)
            .update("/annualPaymentsMade/reliefClaimed", invalidValue)

        val result: Either[ErrorWrapper, AmendOtherReliefsRequestData] =
          validator(validNino, validTaxYear, invalidBody).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(
              List(
                LenderNameFormatError.withPath("/qualifyingLoanInterestPayments/0/lenderName"),
                CustomerReferenceFormatError.withPaths(Seq(
                  "/nonDeductibleLoanInterest/customerReference",
                  "/payrollGiving/customerReference",
                  "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
                  "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
                  "/maintenancePayments/0/customerReference",
                  "/annualPaymentsMade/customerReference",
                  "/qualifyingLoanInterestPayments/0/customerReference"
                )),
                IncomeSourceFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource"),
                BusinessNameFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/businessName"),
                NatureOfTradeFormatError.withPath("/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade"),
                ValueFormatError.withPaths(Seq(
                  "/nonDeductibleLoanInterest/reliefClaimed",
                  "/payrollGiving/reliefClaimed",
                  "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
                  "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
                  "/maintenancePayments/0/amount",
                  "/annualPaymentsMade/reliefClaimed",
                  "/qualifyingLoanInterestPayments/0/reliefClaimed"
                )),
                DateFormatError.withPaths(
                  Seq(
                    "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased",
                    "/maintenancePayments/0/exSpouseDateOfBirth"
                  )),
                ExSpouseNameFormatError.withPath("/maintenancePayments/0/exSpouseName")
              )
            )
          ))

      }

    }
  }

}
