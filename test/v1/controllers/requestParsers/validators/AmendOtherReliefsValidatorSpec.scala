/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors.{CustomerReferenceFormatError, ReliefDateFormatError, ValueFormatError, NinoFormatError, RuleIncorrectOrEmptyBodyError, RuleTaxYearRangeInvalidError, TaxYearFormatError}
import v1.models.request.amendOtherReliefs.AmendOtherReliefsRawData

class AmendOtherReliefsValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTaxYear = "2018-19"
  private val requestBodyJson = Json.parse(
    """
      |{
      |  "nonDeductableLoanInterest": {
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
      |  "maintenancePayments": [
      |    {
      |      "customerReference": "myref",
      |      "exSpouseName" : "Hilda",
      |      "exSpouseDateOfBirth": "2000-01-01",
      |      "amount": 222.22
      |    }
      |  ],
      |  "postCessationTradeReliefAndCertainOtherLosses": [
      |    {
      |      "customerReference": "myref",
      |      "businessName": "ACME Inc",
      |      "dateBusinessCeased": "2019-08-10",
      |      "natureOfTrade": "Widgets Manufacturer",
      |      "incomeSource": "AB12412/A12",
      |      "amount": 222.22
      |    }
      |  ],
      |  "annualPaymentsMade": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 763.00
      |  },
      |  "qualifyingLoanInterestPayments": [
      |    {
      |      "customerReference": "myref",
      |      "lenderName": "Maurice",
      |      "reliefClaimed": 763.00
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  private val emptyJson = Json.parse(
    """
      |{}
      |""".stripMargin
  )

  private val nonDeductableLoanInterestJson = Json.parse(
    """
      |{
      |  "nonDeductableLoanInterest": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 763.00
      |  }
      |}
      |""".stripMargin
  )

  private val payrollGivingJson = Json.parse(
    """
      |{
      |  "payrollGiving": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 154.00
      |  }
      |}
      |""".stripMargin
  )

  private val qualifyingDistributionRedemptionOfSharesAndSecuritiesJson = Json.parse(
    """
      |{
      | "maintenancePayments": [
      |    {
      |      "customerReference": "myref",
      |      "exSpouseName" : "Hilda",
      |      "exSpouseDateOfBirth": "2000-01-01",
      |      "amount": 222.22
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  private val maintenancePaymentsJson = Json.parse(
    """
      |{
      |  "nonDeductableLoanInterest": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 763.00
      |  }
      |}
      |""".stripMargin
  )

  private val postCessationTradeReliefAndCertainOtherLossesJson = Json.parse(
    """
      |{
      |  "postCessationTradeReliefAndCertainOtherLosses": [
      |    {
      |      "customerReference": "myref",
      |      "businessName": "ACME Inc",
      |      "dateBusinessCeased": "2019-08-10",
      |      "natureOfTrade": "Widgets Manufacturer",
      |      "incomeSource": "AB12412/A12",
      |      "amount": 222.22
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  private val annualPaymentsMadeJson = Json.parse(
    """
      |{
      |  "annualPaymentsMade": {
      |    "customerReference": "myref",
      |    "reliefClaimed": 763.00
      |  }
      |}
      |""".stripMargin
  )

  private val qualifyingLoanInterestPaymentsJson = Json.parse(
    """
      |{
      |  "qualifyingLoanInterestPayments": [
      |    {
      |      "customerReference": "myref",
      |      "lenderName": "Maurice",
      |      "reliefClaimed": 763.00
      |    }
      |  ]
      |}
      |""".stripMargin
  )


  val validator = new AmendOtherReliefsValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, requestBodyJson)) shouldBe Nil
      }
      "a valid request with the nonDeductableLoanInterest field is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, nonDeductableLoanInterestJson)) shouldBe Nil
      }
      "a valid request with the payrollGiving field is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, payrollGivingJson)) shouldBe Nil
      }
      "a valid request with the qualifyingDistributionRedemptionOfSharesAndSecurities field is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, qualifyingDistributionRedemptionOfSharesAndSecuritiesJson)) shouldBe Nil
      }
      "a valid request with the maintenancePayments field is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, maintenancePaymentsJson)) shouldBe Nil
      }
      "a valid request with the postCessationTradeReliefAndCertainOtherLosses field is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, postCessationTradeReliefAndCertainOtherLossesJson)) shouldBe Nil
      }
      "a valid request with the annualPaymentsMade field is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, annualPaymentsMadeJson)) shouldBe Nil
      }
      "a valid request with the qualifyingLoanInterestPayments field is supplied" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, qualifyingLoanInterestPaymentsJson)) shouldBe Nil
      }
    }

    "return a path parameter error" when {
      "the nino is invalid" in {
        validator.validate(AmendOtherReliefsRawData("Walrus", validTaxYear, requestBodyJson)) shouldBe List(NinoFormatError)
      }
      "the taxYear format is invalid" in {
        validator.validate(AmendOtherReliefsRawData(validNino, "2000", requestBodyJson)) shouldBe List(TaxYearFormatError)
      }
      "the taxYear range is invalid" in {
        validator.validate(AmendOtherReliefsRawData(validNino, "2017-20", requestBodyJson)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "all path parameters are invalid" in {
        validator.validate(AmendOtherReliefsRawData("Walrus", "2000", requestBodyJson)) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
    }

    "return RuleIncorrectOrEmptyBodyError error" when {
      "an empty JSON body is submitted" in {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, emptyJson)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "at least one empty array is provided" in {
        val json = Json.parse(
            """
              |{
              |  "nonDeductableLoanInterest": {
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
              |  "maintenancePayments": [],
              |  "postCessationTradeReliefAndCertainOtherLosses": [
              |    {
              |      "customerReference": "myref",
              |      "businessName": "ACME Inc",
              |      "dateBusinessCeased": "2019-08-10",
              |      "natureOfTrade": "Widgets Manufacturer",
              |      "incomeSource": "AB12412/A12",
              |      "amount": 222.22
              |    }
              |  ],
              |  "annualPaymentsMade": {
              |    "customerReference": "myref",
              |    "reliefClaimed": 763.00
              |  },
              |  "qualifyingLoanInterestPayments": [
              |    {
              |      "customerReference": "myref",
              |      "lenderName": "Maurice",
              |      "reliefClaimed": 763.00
              |    }
              |  ]
              |}
              |""".stripMargin)
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, json)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "at least one array contains an empty object" in {
        val json = Json.parse(
          """
            |{
            |  "nonDeductableLoanInterest": {
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
            |  "maintenancePayments": [
            |    {}
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |      "customerReference": "myref",
            |      "businessName": "ACME Inc",
            |      "dateBusinessCeased": "2019-08-10",
            |      "natureOfTrade": "Widgets Manufacturer",
            |      "incomeSource": "AB12412/A12",
            |      "amount": 222.22
            |    }
            |  ],
            |  "annualPaymentsMade": {
            |    "customerReference": "myref",
            |    "reliefClaimed": 763.00
            |  },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |      "customerReference": "myref",
            |      "lenderName": "Maurice",
            |      "reliefClaimed": 763.00
            |    }
            |  ]
            |}
            |""".stripMargin)
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, json)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }

    "return a customerReference format error" when {
      "the customerReference provided is invalid" in {
        val badJson = Json.parse(
          """
            |{
            |  "nonDeductableLoanInterest": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "reliefClaimed": 763.00
            |  },
            |  "payrollGiving": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "reliefClaimed": 154.00
            |  },
            |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "amount": 222.22
            |  },
            |  "maintenancePayments": [
            |    {
            |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAA",
            |      "exSpouseName" : "Hilda",
            |      "exSpouseDateOfBirth": "2000-01-01",
            |      "amount": 222.22
            |    }
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAA",
            |      "businessName": "ACME Inc",
            |      "dateBusinessCeased": "2019-08-10",
            |      "natureOfTrade": "Widgets Manufacturer",
            |      "incomeSource": "AB12412/A12",
            |      "amount": 222.22
            |    }
            |  ],
            |  "annualPaymentsMade": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "reliefClaimed": 763.00
            |  },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAA",
            |      "lenderName": "Maurice",
            |      "reliefClaimed": 763.00
            |    }
            |  ]
            |}
            |""".stripMargin)
          validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
            CustomerReferenceFormatError.copy(paths = Some(Seq(
              "/nonDeductableLoanInterest/customerReference",
              "/payrollGiving/customerReference",
              "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
              "/maintenancePayments/0/customerReference",
              "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
              "/annualPaymentsMade/customerReference",
              "/qualifyingLoanInterestPayments/0/customerReference"
            )))
          )
      }
    }
    "return a FORMAT_VALUE error" when {
      "all fields are below 0" in {
        val badJson = Json.parse(
          """
            |{
            |  "nonDeductableLoanInterest": {
            |    "customerReference": "myref",
            |    "reliefClaimed": -1.00
            |  },
            |  "payrollGiving": {
            |    "customerReference": "myref",
            |    "reliefClaimed": -1.00
            |  },
            |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
            |    "customerReference": "myref",
            |    "amount": -1.00
            |  },
            |  "maintenancePayments": [
            |    {
            |      "customerReference": "myref",
            |      "exSpouseName" : "Hilda",
            |      "exSpouseDateOfBirth": "2000-01-01",
            |      "amount": -1.00
            |    },
            |    {
            |      "customerReference": "myref",
            |      "exSpouseName" : "Hilda",
            |      "exSpouseDateOfBirth": "2000-01-01",
            |      "amount": -1.00
            |    }
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |      "customerReference": "myref",
            |      "businessName": "ACME Inc",
            |      "dateBusinessCeased": "2019-08-10",
            |      "natureOfTrade": "Widgets Manufacturer",
            |      "incomeSource": "AB12412/A12",
            |      "amount": -1.00
            |    }
            |  ],
            |  "annualPaymentsMade": {
            |    "customerReference": "myref",
            |    "reliefClaimed": -1.00
            |  },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |      "customerReference": "myref",
            |      "lenderName": "Maurice",
            |      "reliefClaimed": -1.00
            |    }
            |  ]
            |}
            |""".stripMargin)
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq(
            "/nonDeductableLoanInterest/reliefClaimed",
            "/payrollGiving/reliefClaimed",
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
            "/maintenancePayments/0/amount",
            "/maintenancePayments/1/amount",
            "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
            "/annualPaymentsMade/reliefClaimed",
            "/qualifyingLoanInterestPayments/0/reliefClaimed"
          )))
        )
      }
      "only some fields are below 0" in {
        val badJson = Json.parse(
          """
            |{
            |  "nonDeductableLoanInterest": {
            |    "customerReference": "myref",
            |    "reliefClaimed": 763.00
            |  },
            |  "payrollGiving": {
            |    "customerReference": "myref",
            |    "reliefClaimed": 763.00
            |  },
            |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
            |    "customerReference": "myref",
            |    "amount": -1.00
            |  },
            |  "maintenancePayments": [
            |    {
            |      "customerReference": "myref",
            |      "exSpouseName" : "Hilda",
            |      "exSpouseDateOfBirth": "2000-01-01",
            |      "amount": -1.00
            |    },
            |    {
            |      "customerReference": "myref",
            |      "exSpouseName" : "Hilda",
            |      "exSpouseDateOfBirth": "2000-01-01",
            |      "amount": 763.00
            |    }
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |      "customerReference": "myref",
            |      "businessName": "ACME Inc",
            |      "dateBusinessCeased": "2019-08-10",
            |      "natureOfTrade": "Widgets Manufacturer",
            |      "incomeSource": "AB12412/A12",
            |      "amount": -1.00
            |    }
            |  ],
            |  "annualPaymentsMade": {
            |    "customerReference": "myref",
            |    "reliefClaimed": -1.00
            |  },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |      "customerReference": "myref",
            |      "lenderName": "Maurice",
            |      "reliefClaimed": 763.00
            |    }
            |  ]
            |}
            |""".stripMargin)
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ValueFormatError.copy(paths = Some(Seq(
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
            "/maintenancePayments/0/amount",
            "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
            "/annualPaymentsMade/reliefClaimed"
          )))
        )
      }
    }
    "return a FORMAT_DATE error" when {
      "the dates are invalid" in {
        val badJson = Json.parse(
          """
            |{
            |  "nonDeductableLoanInterest": {
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
            |  "maintenancePayments": [
            |    {
            |      "customerReference": "myref",
            |      "exSpouseName" : "Hilda",
            |      "exSpouseDateOfBirth": "2000",
            |      "amount": 222.22
            |    }
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |      "customerReference": "myref",
            |      "businessName": "ACME Inc",
            |      "dateBusinessCeased": "01-01-2020",
            |      "natureOfTrade": "Widgets Manufacturer",
            |      "incomeSource": "AB12412/A12",
            |      "amount": 222.22
            |    }
            |  ],
            |  "annualPaymentsMade": {
            |    "customerReference": "myref",
            |    "reliefClaimed": 763.00
            |  },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |      "customerReference": "myref",
            |      "lenderName": "Maurice",
            |      "reliefClaimed": 763.00
            |    }
            |  ]
            |}
            |""".stripMargin)
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ReliefDateFormatError.copy(paths = Some(Seq(
            "/maintenancePayments/0/exSpouseDateOfBirth",
            "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
          )))
        )
      }
    }
    "return all types of errors" when {
      "the provided data violates all errors" in {
        val badJson = Json.parse(
          """
            |{
            |  "nonDeductableLoanInterest": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "reliefClaimed": -1.00
            |  },
            |  "payrollGiving": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "reliefClaimed": -1.00
            |  },
            |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "amount": -1.00
            |  },
            |  "maintenancePayments": [
            |    {
            |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            |      "exSpouseName" : "Hilda",
            |      "exSpouseDateOfBirth": "01-01-230",
            |      "amount": -1.00
            |    }
            |  ],
            |  "postCessationTradeReliefAndCertainOtherLosses": [
            |    {
            |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            |      "businessName": "ACME Inc",
            |      "dateBusinessCeased": "01-01-230",
            |      "natureOfTrade": "Widgets Manufacturer",
            |      "incomeSource": "AB12412/A12",
            |      "amount": -1.00
            |    }
            |  ],
            |  "annualPaymentsMade": {
            |    "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            |    "reliefClaimed": -1.00
            |  },
            |  "qualifyingLoanInterestPayments": [
            |    {
            |      "customerReference": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
            |      "lenderName": "Maurice",
            |      "reliefClaimed": -1.00
            |    }
            |  ]
            |}
            |""".stripMargin)

        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          CustomerReferenceFormatError.copy(paths = Some(Seq(
            "/nonDeductableLoanInterest/customerReference",
            "/payrollGiving/customerReference",
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
            "/maintenancePayments/0/customerReference",
            "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
            "/annualPaymentsMade/customerReference",
            "/qualifyingLoanInterestPayments/0/customerReference"
          ))),
          ValueFormatError.copy(paths = Some(Seq(
            "/nonDeductableLoanInterest/reliefClaimed",
            "/payrollGiving/reliefClaimed",
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
            "/maintenancePayments/0/amount",
            "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
            "/annualPaymentsMade/reliefClaimed",
            "/qualifyingLoanInterestPayments/0/reliefClaimed"
          ))),
          ReliefDateFormatError.copy(paths = Some(Seq(
            "/maintenancePayments/0/exSpouseDateOfBirth",
            "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
          )))
        )
      }
    }
  }
}
