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

package v1.models.audit

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors.NinoFormatError

class AmendPensionsReliefsAuditDetailSpec extends UnitSpec {

  val validJson = Json.parse(
    """{
      |    "userType": "Agent",
      |    "agentReferenceNumber":"012345678",
      |    "nino": "ZG903729C",
      |    "taxYear" : "2019-20",
      |    "request": {
      |        "vctSubscription":[
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
      |    },
      |    "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
      |    "response": {
      |      "httpStatus": 200,
      |      "body": {
      |        "links": [
      |          {
      |            "href": "/individuals/reliefs/pensions/{nino}/{taxYear}",
      |            "rel": "amend-reliefs-pensions",
      |            "method": "PUT"
      |          },
      |          {
      |            "href": "/individuals/reliefs/pensions/{nino}/{taxYear}",
      |            "rel": "self",
      |            "method": "GET"
      |          },
      |          {
      |            "href": "/individuals/reliefs/pensions/{nino}/{taxYear}",
      |            "rel": "delete-reliefs-pensions",
      |            "method": "DELETE"
      |          }
      |        ]
      |      }
      |    }
      |}""".stripMargin)

  val validBody = AmendPensionsReliefsAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = "ZG903729C",
    taxYear = "2019-20",
    request = Json.parse(
      """
        |{
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
        """.stripMargin
    ),
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
    response = AuditResponse(
      200,
      Right(Some(Json.parse(
        """{
          |"links": [
          |          {
          |            "href": "/individuals/reliefs/pensions/{nino}/{taxYear}",
          |            "rel": "amend-reliefs-pensions",
          |            "method": "PUT"
          |          },
          |          {
          |            "href": "/individuals/reliefs/pensions/{nino}/{taxYear}",
          |            "rel": "self",
          |            "method": "GET"
          |          },
          |          {
          |            "href": "/individuals/reliefs/pensions/{nino}/{taxYear}",
          |            "rel": "delete-reliefs-pensions",
          |            "method": "DELETE"
          |          }
          |        ]
          |}""".stripMargin)))
    )
  )

  val invalidNinoJson = Json.parse(
    """{
      |    "userType": "Agent",
      |    "agentReferenceNumber":"012345678",
      |    "nino": "notANino",
      |    "taxYear" : "2019-20",
      |    "request": {
      |        "vctSubscription":[
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
      |    },
      |    "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
      |    "response": {
      |      "httpStatus": 400,
      |      "errors": [
      |        {
      |          "errorCode":"FORMAT_NINO"
      |        }
      |      ]
      |    }
      |}""".stripMargin)

  val invalidNinoBody = AmendPensionsReliefsAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = "notANino",
    taxYear = "2019-20",
    request = Json.parse(
      """
        |{
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
        """.stripMargin
    ),
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253",
    response = AuditResponse(400, Left(Seq(AuditError(NinoFormatError.code))))
  )

  "writes" must {
    "work" when {
      "success response" in {
        Json.toJson(validBody) shouldBe validJson
      }
    }
    "work" when {
      "error response" in {
        Json.toJson(invalidNinoBody) shouldBe invalidNinoJson
      }
    }
  }
}
