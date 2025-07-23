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

package v3.reliefInvestments.createAmend.def2.model

import play.api.libs.json.{JsValue, Json}
import v3.reliefInvestments.createAmend.def2.model.request._

object Def2_CreateAndAmendReliefInvestmentsFixtures {

  val Def2_vctSubscriptionsItemModel: VctSubscriptionsItem = VctSubscriptionsItem(
    uniqueInvestmentRef = "VCTREF",
    name = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val Def2_vctSubscriptionsItemJson: JsValue = Json.parse(
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

  val Def2_eisSubscriptionsItemModel: EisSubscriptionsItem = EisSubscriptionsItem(
    uniqueInvestmentRef = "XTAL",
    name = Some("EIS Fund X"),
    knowledgeIntensive = Some(true),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(43432.00)
  )

  val Def2_eisSubscriptionsItemJson: JsValue = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "XTAL",
      |  "name": "EIS Fund X",
      |  "knowledgeIntensive": true,
      |  "dateOfInvestment": "2018-04-16",
      |  "amountInvested": 23312.00,
      |  "reliefClaimed": 43432.00
      |}
        """.stripMargin
  )

  val Def2_communityInvestmentItemModel: CommunityInvestmentItem = CommunityInvestmentItem(
    uniqueInvestmentRef = "VCTREF",
    name = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val Def2_communityInvestmentItemJson: JsValue = Json.parse(
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

  val Def2_seedEnterpriseInvestmentItemModel: SeedEnterpriseInvestmentItem = SeedEnterpriseInvestmentItem(
    uniqueInvestmentRef = "1234121A",
    companyName = Some("Company Inc"),
    dateOfInvestment = Some("2020-12-12"),
    amountInvested = Some(BigDecimal(123123.22)),
    reliefClaimed = BigDecimal(3432.00)
  )

  val Def2_seedEnterpriseInvestmentItemJson: JsValue = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "1234121A",
      |  "companyName": "Company Inc",
      |  "dateOfInvestment": "2020-12-12",
      |  "amountInvested": 123123.22,
      |  "reliefClaimed": 3432.00
      |}
        """.stripMargin
  )

  val Def2_requestBodyModel: Def2_CreateAndAmendReliefInvestmentsRequestBody = Def2_CreateAndAmendReliefInvestmentsRequestBody(
    vctSubscription = Some(Seq(Def2_vctSubscriptionsItemModel)),
    eisSubscription = Some(Seq(Def2_eisSubscriptionsItemModel)),
    communityInvestment = Some(Seq(Def2_communityInvestmentItemModel)),
    seedEnterpriseInvestment = Some(Seq(Def2_seedEnterpriseInvestmentItemModel))
  )

  val Def2_requestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "vctSubscription":[$Def2_vctSubscriptionsItemJson],
      |  "eisSubscription":[$Def2_eisSubscriptionsItemJson],
      |  "communityInvestment": [$Def2_communityInvestmentItemJson],
      |  "seedEnterpriseInvestment": [$Def2_seedEnterpriseInvestmentItemJson]
      |}
        """.stripMargin
  )

}
