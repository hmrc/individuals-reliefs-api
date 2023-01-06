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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, JsValue, OWrites}
import v1.models.auth.UserDetails

case class CharitableGivingReliefAuditDetail(versionNumber: String,
                                             userType: String,
                                             agentReferenceNumber: Option[String],
                                             nino: String,
                                             taxYear: String,
                                             requestBody: Option[JsValue],
                                             `X-CorrelationId`: String,
                                             response: String,
                                             httpStatusCode: Int,
                                             errorCodes: Option[Seq[String]])

object CharitableGivingReliefAuditDetail {

  implicit val writes: OWrites[CharitableGivingReliefAuditDetail] = (
    (JsPath \ "versionNumber").write[String] and
      (JsPath \ "userType").write[String] and
      (JsPath \ "agentReferenceNumber").writeNullable[String] and
      (JsPath \ "nino").write[String] and
      (JsPath \ "taxYear").write[String] and
      JsPath.writeNullable[JsValue] and
      (JsPath \ "X-CorrelationId").write[String] and
      (JsPath \ "response").write[String] and
      (JsPath \ "httpStatusCode").write[Int] and
      (JsPath \ "errorCodes").writeNullable[Seq[String]]
  )(unlift(CharitableGivingReliefAuditDetail.unapply))

  def apply(userDetails: UserDetails,
            nino: String,
            taxYear: String,
            requestBody: Option[JsValue],
            `X-CorrelationId`: String,
            auditResponse: AuditResponse): CharitableGivingReliefAuditDetail = {

    val resOutcome: String = if (auditResponse.errors.exists(_.nonEmpty)) "error" else "success"
    val errorCodes: Option[Seq[String]] = auditResponse.errors.flatMap {
      case Nil  => None
      case errs => Some(errs.map(_.errorCode))
    }

    CharitableGivingReliefAuditDetail(
      versionNumber = "1.0",
      userType = userDetails.userType,
      agentReferenceNumber = userDetails.agentReferenceNumber,
      nino = nino,
      taxYear = taxYear,
      requestBody = requestBody,
      `X-CorrelationId` = `X-CorrelationId`,
      response = resOutcome,
      httpStatusCode = auditResponse.httpStatus,
      errorCodes = errorCodes
    )
  }

}
