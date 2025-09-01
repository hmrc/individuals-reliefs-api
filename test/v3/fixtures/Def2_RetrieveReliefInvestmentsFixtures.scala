/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.fixtures

import play.api.libs.json.{JsValue, Json}
import shared.models.domain.Timestamp
import v3.reliefInvestments.retrieve.def2.model.response._

object Def2_RetrieveReliefInvestmentsFixtures {

  val vctSubscriptionsItemModel: VctSubscriptionsItem = VctSubscriptionsItem(
    uniqueInvestmentRef = Some("VCTREF"),
    name = "VCT Fund X",
    dateOfInvestment = "2018-04-16",
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val vctSubscriptionsItemJson: JsValue = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "VCTREF",
      |  "name": "VCT Fund X",
      |  "dateOfInvestment": "2018-04-16",
      |  "amountInvested": 23312.00,
      |  "reliefClaimed": 1334.00
      |}
        """.stripMargin
  )

  val eisSubscriptionsItemModel: EisSubscriptionsItem = EisSubscriptionsItem(
    uniqueInvestmentRef = "XTAL",
    name = "EIS Fund X",
    knowledgeIntensive = true,
    dateOfInvestment = "2020-12-12",
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(43432.00)
  )

  val eisSubscriptionsItemJson: JsValue =
    Json
      .parse(s"""
           |{
           |  "uniqueInvestmentRef": "XTAL",
           |  "name": "EIS Fund X",
           |  "knowledgeIntensive": true,
           |  "dateOfInvestment": "2020-12-12",
           |  "amountInvested": 23312.00,
           |  "reliefClaimed": 43432.00
           |}
           |""".stripMargin)

  val communityInvestmentItemModel: CommunityInvestmentItem = CommunityInvestmentItem(
    uniqueInvestmentRef = "VCTREF",
    name = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val communityInvestmentItemJson: JsValue = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "VCTREF",
      |  "name": "VCT Fund X",
      |  "dateOfInvestment": "2018-04-16",
      |  "amountInvested": 23312.00,
      |  "reliefClaimed": 1334.00
      |}
        """.stripMargin
  )

  val seedEnterpriseInvestmentItemModel: SeedEnterpriseInvestmentItem = SeedEnterpriseInvestmentItem(
    uniqueInvestmentRef = "123412/1A",
    companyName = "Company Inc",
    dateOfInvestment = "2020-12-12",
    amountInvested = Some(BigDecimal(123123.22)),
    reliefClaimed = BigDecimal(3432.00)
  )

  val seedEnterpriseInvestmentItemJson: JsValue = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "123412/1A",
      |  "companyName": "Company Inc",
      |  "dateOfInvestment": "2020-12-12",
      |  "amountInvested": 123123.22,
      |  "reliefClaimed": 3432.00
      |}
        """.stripMargin
  )

  val responseModel: Def2_RetrieveReliefInvestmentsResponse = Def2_RetrieveReliefInvestmentsResponse(
    submittedOn = Timestamp("2020-06-17T10:53:38.000Z"),
    vctSubscription = Some(Seq(vctSubscriptionsItemModel)),
    eisSubscription = Some(Seq(eisSubscriptionsItemModel)),
    communityInvestment = Some(Seq(communityInvestmentItemModel)),
    seedEnterpriseInvestment = Some(Seq(seedEnterpriseInvestmentItemModel))
  )

  val responseJson: JsValue = Json.parse(
    s"""
      |{
      |  "submittedOn": "2020-06-17T10:53:38.000Z",
      |  "vctSubscription":[$vctSubscriptionsItemJson],
      |  "eisSubscription":[$eisSubscriptionsItemJson],
      |  "communityInvestment": [$communityInvestmentItemJson],
      |  "seedEnterpriseInvestment": [$seedEnterpriseInvestmentItemJson]
      |}
        """.stripMargin
  )

}
