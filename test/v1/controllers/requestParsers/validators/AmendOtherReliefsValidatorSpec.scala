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

package v1.controllers.requestParsers.validators

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors._
import v1.models.request.amendOtherReliefs.AmendOtherReliefsRawData

class AmendOtherReliefsValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino = "AA123456A"
  private val validTaxYear = "2021-22"
  private val requestBodyJson = Json.parse(
    """
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

  private val nonDeductibleLoanInterestJson = Json.parse(
    """
      |{
      |  "nonDeductibleLoanInterest": {
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
      |  "nonDeductibleLoanInterest": {
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


  class Test {
    val validator = new AmendOtherReliefsValidator(mockAppConfig)
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, requestBodyJson)) shouldBe Nil
      }
      "a valid request with the nonDeductibleLoanInterest field is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, nonDeductibleLoanInterestJson)) shouldBe Nil
      }
      "a valid request with the payrollGiving field is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, payrollGivingJson)) shouldBe Nil
      }
      "a valid request with the qualifyingDistributionRedemptionOfSharesAndSecurities field is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, qualifyingDistributionRedemptionOfSharesAndSecuritiesJson)) shouldBe Nil
      }
      "a valid request with the maintenancePayments field is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, maintenancePaymentsJson)) shouldBe Nil
      }
      "a valid request with the postCessationTradeReliefAndCertainOtherLosses field is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, postCessationTradeReliefAndCertainOtherLossesJson)) shouldBe Nil
      }
      "a valid request with the annualPaymentsMade field is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, annualPaymentsMadeJson)) shouldBe Nil
      }
      "a valid request with the qualifyingLoanInterestPayments field is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, qualifyingLoanInterestPaymentsJson)) shouldBe Nil
      }
    }

    "return a path parameter error" when {
      "the nino is invalid" in new Test {
        validator.validate(AmendOtherReliefsRawData("Walrus", validTaxYear, requestBodyJson)) shouldBe List(NinoFormatError)
      }
      "the taxYear format is invalid" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, "2000", requestBodyJson)) shouldBe List(TaxYearFormatError)
      }
      "the taxYear range is invalid" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, "2017-20", requestBodyJson)) shouldBe List(RuleTaxYearRangeInvalidError)
      }
      "a tax year before the earliest allowed date is supplied" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, "2019-20", requestBodyJson)) shouldBe List(RuleTaxYearNotSupportedError)
      }
      "all path parameters are invalid" in new Test {
        validator.validate(AmendOtherReliefsRawData("Walrus", "2000", requestBodyJson)) shouldBe List(NinoFormatError, TaxYearFormatError)
      }
    }

    "return a RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED error" when {
      "an empty JSON body is submitted" in new Test {
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, emptyJson)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
      "at least one empty array is provided" in new Test {
        val json = Json.parse(
          """
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
      "at least one array contains an empty object" in new Test {
        val json = Json.parse(
          """
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

    "return a FORMAT_CUSTOMER_REF error" when {
      "the customerReference provided is invalid" in new Test {
        val badJson = Json.parse(
          s"""
             |{
             |  "nonDeductibleLoanInterest": {
             |    "customerReference": "${("1234567890" * 9) + "1"}",
             |    "reliefClaimed": 763.00
             |  },
             |  "payrollGiving": {
             |    "customerReference": "${("1234567890" * 9) + "1"}",
             |    "reliefClaimed": 154.00
             |  },
             |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
             |    "customerReference": "${("1234567890" * 9) + "1"}",
             |    "amount": 222.22
             |  },
             |  "maintenancePayments": [
             |    {
             |      "customerReference": "${("1234567890" * 9) + "1"}",
             |      "exSpouseName" : "Hilda",
             |      "exSpouseDateOfBirth": "2000-01-01",
             |      "amount": 222.22
             |    }
             |  ],
             |  "postCessationTradeReliefAndCertainOtherLosses": [
             |    {
             |      "customerReference": "${("1234567890" * 9) + "1"}",
             |      "businessName": "ACME Inc",
             |      "dateBusinessCeased": "2019-08-10",
             |      "natureOfTrade": "Widgets Manufacturer",
             |      "incomeSource": "AB12412/A12",
             |      "amount": 222.22
             |    }
             |  ],
             |  "annualPaymentsMade": {
             |    "customerReference": "${("1234567890" * 9) + "1"}",
             |    "reliefClaimed": 763.00
             |  },
             |  "qualifyingLoanInterestPayments": [
             |    {
             |      "customerReference": "${("1234567890" * 9) + "1"}",
             |      "lenderName": "Maurice",
             |      "reliefClaimed": 763.00
             |    }
             |  ]
             |}
             |""".stripMargin)
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          CustomerReferenceFormatError.copy(paths = Some(Seq(
            "/nonDeductibleLoanInterest/customerReference",
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

    "return a FORMAT_NAME_EX_SPOUSE error" when {
      "the exSpouseName provided is invalid" in new Test {
        val badJson = Json.parse(
          """
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
            |  "maintenancePayments": [
            |    {
            |      "customerReference": "myref",
            |      "exSpouseName" : "",
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
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          ExSpouseNameFormatError.copy(paths = Some(Seq(
            "/maintenancePayments/0/exSpouseName"
          )))
        )
      }
    }

    "return a FORMAT_NAME_BUSINESS error" when {
      "the businessName provided is invalid" in new Test {
        val badJson = Json.parse(
          """
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
            |      "businessName": "",
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
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          BusinessNameFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/businessName"
          )))
        )
      }
    }

    "return a FORMAT_NATURE_OF_TRADE error" when {
      "the natureOfTrade provided is invalid" in new Test {
        val badJson = Json.parse(
          """
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
            |      "businessName": "My Business",
            |      "dateBusinessCeased": "2019-08-10",
            |      "natureOfTrade": "",
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
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          NatureOfTradeFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade"
          )))
        )
      }
    }

    "return a FORMAT_INCOME_SOURCE error" when {
      "the incomeSource provided is invalid" in new Test {
        val badJson = Json.parse(
          """
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
            |      "businessName": "My Business",
            |      "dateBusinessCeased": "2019-08-10",
            |      "natureOfTrade": "Widgets Manufacturer",
            |      "incomeSource": "",
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
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          IncomeSourceFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource"
          )))
        )
      }
    }

    "return a FORMAT_LENDER_NAME error" when {
      "the lenderName provided is invalid" in new Test {
        val badJson = Json.parse(
          """
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
            |      "businessName": "My Business",
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
            |      "lenderName": "",
            |      "reliefClaimed": 763.00
            |    }
            |  ]
            |}
            |""".stripMargin
        )
        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          LenderNameFormatError.copy(paths = Some(Seq(
            "/qualifyingLoanInterestPayments/0/lenderName"
          )))
        )
      }
    }

    "return a FORMAT_VALUE error" when {
      "all fields are below 0" in new Test {
        val badJson = Json.parse(
          """
            |{
            |  "nonDeductibleLoanInterest": {
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
            "/nonDeductibleLoanInterest/reliefClaimed",
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
      "only some fields are below 0" in new Test {
        val badJson = Json.parse(
          """
            |{
            |  "nonDeductibleLoanInterest": {
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
      "the dates are invalid" in new Test {
        val badJson = Json.parse(
          """
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
          DateFormatError.copy(paths = Some(Seq(
            "/maintenancePayments/0/exSpouseDateOfBirth",
            "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
          )))
        )
      }
    }

    "return all types of errors" when {
      "the provided data violates all errors" in new Test {
        val badJson = Json.parse(
          s"""
             |{
             |  "nonDeductibleLoanInterest": {
             |    "customerReference": "",
             |    "reliefClaimed": -1.00
             |  },
             |  "payrollGiving": {
             |    "customerReference": "",
             |    "reliefClaimed": -1.00
             |  },
             |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
             |    "customerReference": "",
             |    "amount": -1.00
             |  },
             |  "maintenancePayments": [
             |    {
             |      "customerReference": "",
             |      "exSpouseName" : "",
             |      "exSpouseDateOfBirth": "01-01-230",
             |      "amount": -1.00
             |    }
             |  ],
             |  "postCessationTradeReliefAndCertainOtherLosses": [
             |    {
             |      "customerReference": "",
             |      "businessName": "",
             |      "dateBusinessCeased": "01-01-230",
             |      "natureOfTrade": "",
             |      "incomeSource": "",
             |      "amount": -1.00
             |    }
             |  ],
             |  "annualPaymentsMade": {
             |    "customerReference": "",
             |    "reliefClaimed": -1.00
             |  },
             |  "qualifyingLoanInterestPayments": [
             |    {
             |      "customerReference": "",
             |      "lenderName": "",
             |      "reliefClaimed": -1.00
             |    }
             |  ]
             |}
             |""".stripMargin)

        validator.validate(AmendOtherReliefsRawData(validNino, validTaxYear, badJson)) shouldBe List(
          LenderNameFormatError.copy(paths = Some(Seq(
            "/qualifyingLoanInterestPayments/0/lenderName"
          ))),
          CustomerReferenceFormatError.copy(paths = Some(Seq(
            "/nonDeductibleLoanInterest/customerReference",
            "/payrollGiving/customerReference",
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/customerReference",
            "/maintenancePayments/0/customerReference",
            "/postCessationTradeReliefAndCertainOtherLosses/0/customerReference",
            "/annualPaymentsMade/customerReference",
            "/qualifyingLoanInterestPayments/0/customerReference"
          ))),
          DateFormatError.copy(paths = Some(Seq(
            "/maintenancePayments/0/exSpouseDateOfBirth",
            "/postCessationTradeReliefAndCertainOtherLosses/0/dateBusinessCeased"
          ))),
          ExSpouseNameFormatError.copy(paths = Some(Seq(
            "/maintenancePayments/0/exSpouseName"
          ))),
          IncomeSourceFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/incomeSource"
          ))),
          BusinessNameFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/businessName"
          ))),
          NatureOfTradeFormatError.copy(paths = Some(Seq(
            "/postCessationTradeReliefAndCertainOtherLosses/0/natureOfTrade"
          ))),
          ValueFormatError.copy(paths = Some(Seq(
            "/nonDeductibleLoanInterest/reliefClaimed",
            "/payrollGiving/reliefClaimed",
            "/qualifyingDistributionRedemptionOfSharesAndSecurities/amount",
            "/maintenancePayments/0/amount",
            "/postCessationTradeReliefAndCertainOtherLosses/0/amount",
            "/annualPaymentsMade/reliefClaimed",
            "/qualifyingLoanInterestPayments/0/reliefClaimed"
          )))
        )
      }
    }
  }
}
