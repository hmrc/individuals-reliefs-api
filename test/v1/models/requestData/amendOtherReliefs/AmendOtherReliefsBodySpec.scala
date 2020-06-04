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

package v1.models.requestData.amendOtherReliefs

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class AmendOtherReliefsBodySpec extends UnitSpec with JsonErrorValidators {
 val amendOtherReliefsBody = AmendOtherReliefsBody(
   Some(NonDeductableLoanInterest(
     Some("myref"),
     763.00)),
   Some(PayrollGiving(
     Some("myref"),
     154.00)),
   Some(QualifyingDistributionRedemptionOfSharesAndSecurities(
     Some("myref"),
     222.22)),
   Some(Seq(MaintenancePayments(
     "myref",
     Some("Hilda"),
     Some("2000-01-01"),
     Some(222.22)))),
   Some(Seq(PostCessationTradeReliefAndCertainOtherLosses(
     "myref",
     Some("ACME Inc"),
     Some("2019-08-10"),
     Some("Widgets Manufacturer"),
     Some("AB12412/A12"),
     Some(222.22)))),
   Some(AnnualPaymentsMade(
     Some("myref"),
     763.00)),
   Some(Seq(QualifyingLoanInterestPayments(
     "myref",
     Some("Maurice"),
     763.00)))
 )

  val json = Json.parse(
    """{
      |  "nonDeductableLoanInterest": {
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

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        amendOtherReliefsBody shouldBe json.as[AmendOtherReliefsBody]
      }
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(amendOtherReliefsBody) shouldBe json
      }
    }
  }
}
