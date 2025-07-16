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

package v3.reliefInvestments.createAmend.def1.model

import play.api.libs.json.{JsValue, Json}
import v3.reliefInvestments.createAmend.def1.model.request._

object Def1_CreateAndAmendReliefInvestmentsFixtures {

  val Def1_vctSubscriptionsItemModel: VctSubscriptionsItem = VctSubscriptionsItem(
    uniqueInvestmentRef = "VCTREF",
    name = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val Def1_vctSubscriptionsItemJson: JsValue = Json.parse(
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

  val Def1_eisSubscriptionsItemModel: EisSubscriptionsItem = EisSubscriptionsItem(
    uniqueInvestmentRef = "XTAL",
    name = Some("EIS Fund X"),
    knowledgeIntensive = Some(true),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(43432.00)
  )

  val Def1_eisSubscriptionsItemJson: JsValue = Json.parse(
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

  val Def1_communityInvestmentItemModel: CommunityInvestmentItem = CommunityInvestmentItem(
    uniqueInvestmentRef = "VCTREF",
    name = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val Def1_communityInvestmentItemJson: JsValue = Json.parse(
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

  val Def1_seedEnterpriseInvestmentItemModel: SeedEnterpriseInvestmentItem = SeedEnterpriseInvestmentItem(
    uniqueInvestmentRef = "1234121A",
    companyName = Some("Company Inc"),
    dateOfInvestment = Some("2020-12-12"),
    amountInvested = Some(BigDecimal(123123.22)),
    reliefClaimed = BigDecimal(3432.00)
  )

  val Def1_seedEnterpriseInvestmentItemJson: JsValue = Json.parse(
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

  val Def1_socialEnterpriseInvestmentItemModel: SocialEnterpriseInvestmentItem = SocialEnterpriseInvestmentItem(
    uniqueInvestmentRef = "VCTREF",
    socialEnterpriseName = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val Def1_socialEnterpriseInvestmentItemJson: JsValue = Json.parse(
    """
      |{
      |  "uniqueInvestmentRef": "VCTREF",
      |  "socialEnterpriseName": "VCT Fund X",
      |  "dateOfInvestment": "2018-04-16",
      |  "amountInvested": 23312.00,
      |  "reliefClaimed": 1334.00
      |}
        """.stripMargin
  )

  val Def1_requestBodyModel: Def1_CreateAndAmendReliefInvestmentsRequestBody = Def1_CreateAndAmendReliefInvestmentsRequestBody(
    vctSubscription = Some(Seq(Def1_vctSubscriptionsItemModel)),
    eisSubscription = Some(Seq(Def1_eisSubscriptionsItemModel)),
    communityInvestment = Some(Seq(Def1_communityInvestmentItemModel)),
    seedEnterpriseInvestment = Some(Seq(Def1_seedEnterpriseInvestmentItemModel)),
    socialEnterpriseInvestment = Some(Seq(Def1_socialEnterpriseInvestmentItemModel))
  )

  val Def1_requestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "vctSubscription":[$Def1_vctSubscriptionsItemJson],
      |  "eisSubscription":[$Def1_eisSubscriptionsItemJson],
      |  "communityInvestment": [$Def1_communityInvestmentItemJson],
      |  "seedEnterpriseInvestment": [$Def1_seedEnterpriseInvestmentItemJson],
      |  "socialEnterpriseInvestment": [$Def1_socialEnterpriseInvestmentItemJson]
      |}
        """.stripMargin
  )

}
