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

package v1.models.audit

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.errors.NinoFormatError

class DeleteForeignReliefsAuditDetailSpec extends UnitSpec {


  val validJson = Json.parse(
    """{
      |    "userType": "Agent",
      |    "agentReferenceNumber":"012345678",
      |    "nino": "ZG903729C",
      |    "taxYear" : "2019-20",
      |    "response":{
      |      "httpStatus": 204
      |    },
      |    "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b478d9f015c253"
      |}""".stripMargin)

  val validBody = DeleteForeignReliefsAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = "ZG903729C",
    taxYear = "2019-20",
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b478d9f015c253",
    response = AuditResponse(
      204,
      errors = None,
      body = None
    )
  )

  val invalidNinoJson = Json.parse(
    """{
      |    "userType": "Agent",
      |    "agentReferenceNumber":"012345678",
      |    "nino": "notANino",
      |    "taxYear" : "2019-20",
      |    "response": {
      |      "httpStatus": 400,
      |      "errors": [
      |        {
      |          "errorCode":"FORMAT_NINO"
      |        }
      |      ]
      |    },
      |    "X-CorrelationId":"a1e8057e-fbbc-47a8-a8b478d9f015c253"
      |}""".stripMargin)

  val invalidNinoBody = DeleteForeignReliefsAuditDetail(
    userType = "Agent",
    agentReferenceNumber = Some("012345678"),
    nino = "notANino",
    taxYear = "2019-20",
    `X-CorrelationId` = "a1e8057e-fbbc-47a8-a8b478d9f015c253",
    response = AuditResponse(
      400,
      errors = Some(Seq(AuditError(NinoFormatError.code))),
      body = None
    )
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
