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

package v1.models.audit

import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{JsArray, JsString, JsValue, Json}
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class CharitableGivingReliefAuditDetailSpec extends UnitSpec with JsonErrorValidators {

  val nino: String                 = "TC663795B"
  val taxYear: String              = "2020-21"
  val userType: String             = "Agent"
  val agentReferenceNumber: String = "012345678"
  val xCorrelationId: String       = "a1e8057e-fbbc-47a8-a8b478d9f015c253"

  val requestBodyJson: JsValue = Json.parse(
    """
     |{
     |   "giftAidPayments": {
     |      "nonUkCharities": {
     |         "charityNames": [
     |            "British Heart Foundation",
     |            "St John Ambulance",
     |            "Cancer Research UK"
     |         ],
     |         "totalAmount": "10000.89"
     |      },
     |      "totalAmount": "10000.89",
     |      "oneOffAmount": "100.78",
     |      "amountTreatedAsPreviousTaxYear": "9999.99",
     |      "amountTreatedAsSpecifiedTaxYear": "7777.99"
     |   }
     |}
   """.stripMargin
  )

  val charitableGivingReliefAuditDetailSuccessJson: JsValue = Json.parse(
    """
     |{
     |   "versionNumber": "1.0",
     |   "userType": "Agent",
     |   "agentReferenceNumber": "012345678",
     |   "nino": "TC663795B",
     |   "taxYear": "2020-21",
     |   "giftAidPayments": {
     |      "nonUkCharities": {
     |         "charityNames": [
     |            "British Heart Foundation",
     |            "St John Ambulance",
     |            "Cancer Research UK"
     |         ],
     |         "totalAmount": "10000.89"
     |      },
     |      "totalAmount": "10000.89",
     |      "oneOffAmount": "100.78",
     |      "amountTreatedAsPreviousTaxYear": "9999.99",
     |      "amountTreatedAsSpecifiedTaxYear": "7777.99"
     |   },
     |   "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b478d9f015c253",
     |   "response": "success",
     |   "httpStatusCode": 200
     |}
   """.stripMargin
  )

  def charitableGivingReliefAuditDetailErrorJson(errorCodes: Seq[String]): JsValue = Json.parse(
    s"""
       |{
       |   "versionNumber": "1.0",
       |   "userType": "Agent",
       |   "agentReferenceNumber": "012345678",
       |   "nino": "TC663795B9182",
       |   "taxYear": "2020-21",
       |   "giftAidPayments": {
       |      "nonUkCharities": {
       |         "charityNames": [
       |            "British Heart Foundation",
       |            "St John Ambulance",
       |            "Cancer Research UK"
       |         ],
       |         "totalAmount": "10000.89"
       |      },
       |      "totalAmount": "10000.89",
       |      "oneOffAmount": "100.78",
       |      "amountTreatedAsPreviousTaxYear": "9999.99",
       |      "amountTreatedAsSpecifiedTaxYear": "7777.99"
       |   },
       |   "X-CorrelationId": "a1e8057e-fbbc-47a8-a8b478d9f015c253",
       |   "response": "error",
       |   "httpStatusCode": 400,
       |   "errorCodes": ${JsArray(errorCodes.map(JsString))}
       |}
     """.stripMargin
  )

  val charitableGivingReliefSuccessModel: CharitableGivingReliefAuditDetail = CharitableGivingReliefAuditDetail(
    versionNumber = "1.0",
    userType = userType,
    agentReferenceNumber = Some(agentReferenceNumber),
    nino = nino,
    taxYear = taxYear,
    requestBody = Some(requestBodyJson),
    `X-CorrelationId` = xCorrelationId,
    response = "success",
    httpStatusCode = OK,
    errorCodes = None
  )

  val charitableGivingReliefErrorModel: CharitableGivingReliefAuditDetail = charitableGivingReliefSuccessModel.copy(
    nino = "TC663795B9182",
    httpStatusCode = BAD_REQUEST,
    response = "error",
    errorCodes = Some(Seq("FORMAT_NINO"))
  )

  "CharitableGivingReliefAuditDetail" when {
    "written to JSON (success)" should {
      "produce the expected JsObject" in {
        Json.toJson(charitableGivingReliefSuccessModel) shouldBe charitableGivingReliefAuditDetailSuccessJson
      }
    }

    "written to JSON (single error)" should {
      "produce the expected JsObject" in {
        Json.toJson(charitableGivingReliefErrorModel) shouldBe charitableGivingReliefAuditDetailErrorJson(Seq("FORMAT_NINO"))
      }
    }

    "written to JSON (multiple errors)" should {
      "produce the expected JsObject" in {
        Json.toJson(charitableGivingReliefErrorModel.copy(taxYear = "2020", errorCodes = Some(Seq("FORMAT_NINO", "FORMAT_TAX_YEAR")))) shouldBe
          charitableGivingReliefAuditDetailErrorJson(Seq("FORMAT_NINO", "FORMAT_TAX_YEAR")).update("/taxYear", JsString("2020"))
      }
    }
  }

}
