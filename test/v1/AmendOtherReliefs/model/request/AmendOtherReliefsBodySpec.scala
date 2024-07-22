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

package v1.AmendOtherReliefs.model.request

import api.models.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec
import v1.AmendOtherReliefs.def1.model.request.{AmendOtherReliefsRequestBody, AnnualPaymentsMade, MaintenancePayments, NonDeductibleLoanInterest, PayrollGiving, PostCessationTradeReliefAndCertainOtherLosses, QualifyingDistributionRedemptionOfSharesAndSecurities, QualifyingLoanInterestPayments}

class AmendOtherReliefsBodySpec extends UnitSpec with JsonErrorValidators {

  private val amendOtherReliefsBody = AmendOtherReliefsRequestBody(
    Some(NonDeductibleLoanInterest(Some("myref"), 763.00)),
    Some(PayrollGiving(Some("myref"), 154.00)),
    Some(QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
    Some(Seq(MaintenancePayments(Some("myref"), Some("Hilda"), Some("2000-01-01"), 222.22))),
    Some(
      Seq(
        PostCessationTradeReliefAndCertainOtherLosses(
          Some("myref"),
          Some("ACME Inc"),
          Some("2019-08-10"),
          Some("Widgets Manufacturer"),
          Some("AB12412/A12"),
          222.22))),
    Some(AnnualPaymentsMade(Some("myref"), 763.00)),
    Some(Seq(QualifyingLoanInterestPayments(Some("myref"), Some("Maurice"), 763.00)))
  )

  private val emptyAmendOtherReliefsBody = AmendOtherReliefsRequestBody(
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  private val json = Json.parse(
    """{
      |  "nonDeductibleLoanInterest": {
      |        "customerReference": "myref",
      |        "reliefClaimed": 763.00
      |      },
      |  "payrollGiving": {
      |        "customerReference": "myref",
      |        "reliefClaimed": 154.00
      |      },
      |  "qualifyingDistributionRedemptionOfSharesAndSecurities": {
      |        "customerReference": "myref",
      |        "amount": 222.22
      |      },
      |  "maintenancePayments": [
      |    {
      |        "customerReference": "myref",
      |        "exSpouseName" : "Hilda",
      |        "exSpouseDateOfBirth": "2000-01-01",
      |        "amount": 222.22
      |      }
      |  ],
      |  "postCessationTradeReliefAndCertainOtherLosses": [
      |    {
      |        "customerReference": "myref",
      |        "businessName": "ACME Inc",
      |        "dateBusinessCeased": "2019-08-10",
      |        "natureOfTrade": "Widgets Manufacturer",
      |        "incomeSource": "AB12412/A12",
      |        "amount": 222.22
      |      }
      |  ],
      |  "annualPaymentsMade": {
      |        "customerReference": "myref",
      |        "reliefClaimed": 763.00
      |      },
      |  "qualifyingLoanInterestPayments": [
      |    {
      |        "customerReference": "myref",
      |        "lenderName": "Maurice",
      |        "reliefClaimed": 763.00
      |      }
      |  ]
      |}""".stripMargin
  )

  private val emptyJson = Json.parse("""{}""")

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        amendOtherReliefsBody shouldBe json.as[AmendOtherReliefsRequestBody]
      }
    }
  }

  "read from empty JSON" should {
    "convert empty MTD JSON into an empty AmendSecuritiesItems object" in {
      emptyAmendOtherReliefsBody shouldBe emptyJson.as[AmendOtherReliefsRequestBody]
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(amendOtherReliefsBody) shouldBe json
      }
    }
  }

  "write from an empty body" when {
    "passed an empty model" should {
      "return an empty JSON" in {
        Json.toJson(emptyAmendOtherReliefsBody) shouldBe emptyJson
      }
    }
  }

  "isIncorrectOrEmptyBodyError" should {
    "return false" when {
      "all arrays are provided, none are empty, no objects in the arrays are empty" in {
        val model = AmendOtherReliefsRequestBody(
          Some(NonDeductibleLoanInterest(Some("myref"), 763.00)),
          Some(PayrollGiving(Some("myref"), 154.00)),
          Some(QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
          Some(Seq(MaintenancePayments(Some("myref"), Some("Hilda"), Some("2000-01-01"), 222.22))),
          Some(
            Seq(
              PostCessationTradeReliefAndCertainOtherLosses(
                Some("myref"),
                Some("ACME Inc"),
                Some("2019-08-10"),
                Some("Widgets Manufacturer"),
                Some("AB12412/A12"),
                222.22))),
          Some(AnnualPaymentsMade(Some("myref"), 763.00)),
          Some(Seq(QualifyingLoanInterestPayments(Some("myref"), Some("Maurice"), 763.00)))
        )
        model.isIncorrectOrEmptyBody shouldBe false
      }
      "only some arrays are provided, none are empty, no objects in the arrays are empty" in {
        val model = AmendOtherReliefsRequestBody(
          None,
          Some(PayrollGiving(Some("myref"), 154.00)),
          Some(QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
          None,
          Some(
            Seq(
              PostCessationTradeReliefAndCertainOtherLosses(
                Some("myref"),
                Some("ACME Inc"),
                Some("2019-08-10"),
                Some("Widgets Manufacturer"),
                Some("AB12412/A12"),
                222.22))),
          Some(AnnualPaymentsMade(Some("myref"), 763.00)),
          Some(Seq(QualifyingLoanInterestPayments(Some("myref"), Some("Maurice"), 763.00)))
        )
        model.isIncorrectOrEmptyBody shouldBe false
      }
    }
    "return true" when {
      "no arrays are provided" in {
        val model = AmendOtherReliefsRequestBody(
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
      "at least one empty array is provided" in {
        val model = AmendOtherReliefsRequestBody(
          Some(NonDeductibleLoanInterest(Some("myref"), 763.00)),
          Some(PayrollGiving(Some("myref"), 154.00)),
          Some(QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
          Some(Seq(MaintenancePayments(Some("myref"), Some("Hilda"), Some("2000-01-01"), 222.22))),
          Some(Seq()),
          Some(AnnualPaymentsMade(Some("myref"), 763.00)),
          Some(Seq())
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
    }
  }

}
