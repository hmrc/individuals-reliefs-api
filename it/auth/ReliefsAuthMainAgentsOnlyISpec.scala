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

package auth

import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import shared.auth.AuthMainAgentsOnlyISpec
import shared.services.DownstreamStub

class ReliefsAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "1.0"

  val supportingAgentsNotAllowedEndpoint = "retrieve-relief-investments"

  private val taxYear = "2021-22"

  val mtdUrl = s"/investment/$nino/$taxYear"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  val downstreamUri: String = s"/income-tax/reliefs/investment/$nino/$taxYear"

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse("""
                 |{
                 |  "submittedOn": "2020-06-17T10:53:38.000Z",
                 |  "vctSubscription":[
                 |    {
                 |      "uniqueInvestmentRef": "VCTREF",
                 |      "name": "VCT Fund X",
                 |      "dateOfInvestment": "2018-04-16",
                 |      "amountInvested": 23312.00,
                 |      "reliefClaimed": 1334.00
                 |      }
                 |  ],
                 |  "eisSubscription":[
                 |    {
                 |      "uniqueInvestmentRef": "XTAL",
                 |      "name": "EIS Fund X",
                 |      "knowledgeIntensive": true,
                 |      "dateOfInvestment": "2020-12-12",
                 |      "amountInvested": 23312.00,
                 |      "reliefClaimed": 43432.00
                 |    }
                 |  ],
                 |  "communityInvestment": [
                 |    {
                 |      "uniqueInvestmentRef": "CIREF",
                 |      "name": "CI X",
                 |      "dateOfInvestment": "2020-12-12",
                 |      "amountInvested": 6442.00,
                 |      "reliefClaimed": 2344.00
                 |    }
                 |  ],
                 |  "seedEnterpriseInvestment": [
                 |    {
                 |      "uniqueInvestmentRef": "123412/1A",
                 |      "companyName": "Company Inc",
                 |      "dateOfInvestment": "2020-12-12",
                 |      "amountInvested": 123123.22,
                 |      "reliefClaimed": 3432.00
                 |    }
                 |  ],
                 |  "socialEnterpriseInvestment": [
                 |    {
                 |      "uniqueInvestmentRef": "123412/1A",
                 |      "socialEnterpriseName": "SE Inc",
                 |      "dateOfInvestment": "2020-12-12",
                 |      "amountInvested": 123123.22,
                 |      "reliefClaimed": 3432.00
                 |    }
                 |  ]
                 |}
    """.stripMargin)
  )

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override val expectedMtdSuccessStatus: Int = OK

}
