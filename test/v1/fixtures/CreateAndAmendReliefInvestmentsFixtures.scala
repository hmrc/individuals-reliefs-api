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

package v1.fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.request.createAndAmendReliefInvestments._

object CreateAndAmendReliefInvestmentsFixtures {

  val vctSubscriptionsItemModel: VctSubscriptionsItem = VctSubscriptionsItem(
    uniqueInvestmentRef = "VCTREF",
    name = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
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
    name = Some("EIS Fund X"),
    knowledgeIntensive = true,
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(43432.00)
  )

  val eisSubscriptionsItemJson: JsValue = Json.parse(
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
    uniqueInvestmentRef = "1234121A",
    companyName = Some("Company Inc"),
    dateOfInvestment = Some("2020-12-12"),
    amountInvested = Some(BigDecimal(123123.22)),
    reliefClaimed = BigDecimal(3432.00)
  )

  val seedEnterpriseInvestmentItemJson: JsValue = Json.parse(
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

  val socialEnterpriseInvestmentItemModel: SocialEnterpriseInvestmentItem = SocialEnterpriseInvestmentItem(
    uniqueInvestmentRef = "VCTREF",
    socialEnterpriseName = Some("VCT Fund X"),
    dateOfInvestment = Some("2018-04-16"),
    amountInvested = Some(BigDecimal(23312.00)),
    reliefClaimed = BigDecimal(1334.00)
  )

  val socialEnterpriseInvestmentItemJson: JsValue = Json.parse(
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

  val requestBodyModel: CreateAndAmendReliefInvestmentsBody = CreateAndAmendReliefInvestmentsBody(
    vctSubscription = Some(Seq(vctSubscriptionsItemModel)),
    eisSubscription = Some(Seq(eisSubscriptionsItemModel)),
    communityInvestment = Some(Seq(communityInvestmentItemModel)),
    seedEnterpriseInvestment = Some(Seq(seedEnterpriseInvestmentItemModel)),
    socialEnterpriseInvestment = Some(Seq(socialEnterpriseInvestmentItemModel))
  )

  val requestBodyJson: JsValue = Json.parse(
    s"""
      |{
      |  "vctSubscription":[$vctSubscriptionsItemJson],
      |  "eisSubscription":[$eisSubscriptionsItemJson],
      |  "communityInvestment": [$communityInvestmentItemJson],
      |  "seedEnterpriseInvestment": [$seedEnterpriseInvestmentItemJson],
      |  "socialEnterpriseInvestment": [$socialEnterpriseInvestmentItemJson]
      |}
        """.stripMargin
  )

  def hateoasResponse(taxYear: String = "2019-20"): JsValue = Json.parse(
    s"""
      |{
      |  "links": [
      |    {
      |      "href": "/individuals/reliefs/investment/AA123456A/$taxYear",
      |      "method": "GET",
      |      "rel": "self"
      |    },
      |    {
      |      "href": "/individuals/reliefs/investment/AA123456A/$taxYear",
      |      "method": "PUT",
      |      "rel": "create-and-amend-reliefs-investments"
      |    },
      |    {
      |      "href": "/individuals/reliefs/investment/AA123456A/$taxYear",
      |      "method": "DELETE",
      |      "rel": "delete-reliefs-investments"
      |    }
      |  ]
      |}
      |""".stripMargin
  )

}
