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

package v1.RetrieveOtherReliefs.def1.response

import api.hateoas.Link
import api.hateoas.Method._
import api.models.domain.Timestamp
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.RetrieveOtherReliefs.def1.model.response.{
  Def1_AnnualPaymentsMade,
  Def1_MaintenancePayments,
  Def1_NonDeductibleLoanInterest,
  Def1_PayrollGiving,
  Def1_PostCessationTradeReliefAndCertainOtherLosses,
  Def1_QualifyingDistributionRedemptionOfSharesAndSecurities,
  Def1_QualifyingLoanInterestPayments
}
import v1.RetrieveOtherReliefs.model.response.{Def1_RetrieveOtherReliefsResponse, RetrieveOtherReliefsHateoasData, RetrieveOtherReliefsResponse}

class RetrieveOtherReliefsResponseSpec extends UnitSpec with MockAppConfig {

  val retrieveOtherReliefsBody: RetrieveOtherReliefsResponse = Def1_RetrieveOtherReliefsResponse(
    Timestamp("2020-06-17T10:53:38.000Z"),
    Some(Def1_NonDeductibleLoanInterest(Some("myref"), 763.00)),
    Some(Def1_PayrollGiving(Some("myref"), 154.00)),
    Some(Def1_QualifyingDistributionRedemptionOfSharesAndSecurities(Some("myref"), 222.22)),
    Some(Seq(Def1_MaintenancePayments(Some("myref"), Some("Hilda"), Some("2000-01-01"), 222.22))),
    Some(
      Seq(
        Def1_PostCessationTradeReliefAndCertainOtherLosses(
          Some("myref"),
          Some("ACME Inc"),
          Some("2019-08-10"),
          Some("Widgets Manufacturer"),
          Some("AB12412/A12"),
          222.22))),
    Some(Def1_AnnualPaymentsMade(Some("myref"), 763.00)),
    Some(Seq(Def1_QualifyingLoanInterestPayments(Some("myref"), Some("Maurice"), 763.00)))
  )

  val json = Json.parse(
    """{
      |  "submittedOn": "2020-06-17T10:53:38.000Z",
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

  val emptyJson = Json.parse("""{}""")

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        retrieveOtherReliefsBody shouldBe json.as[Def1_RetrieveOtherReliefsResponse]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(retrieveOtherReliefsBody) shouldBe json
      }
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"

      MockedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()
      RetrieveOtherReliefsResponse.LinksFactory.links(mockAppConfig, RetrieveOtherReliefsHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/my/context/other/$nino/$taxYear", GET, "self"),
          api.hateoas.Link(s"/my/context/other/$nino/$taxYear", PUT, "create-and-amend-reliefs-other"),
          api.hateoas.Link(s"/my/context/other/$nino/$taxYear", DELETE, "delete-reliefs-other")
        )
    }
  }

}
