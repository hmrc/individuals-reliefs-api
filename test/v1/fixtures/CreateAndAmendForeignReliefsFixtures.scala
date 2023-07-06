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
import v1.models.request.createAndAmendForeignReliefs.{CreateAndAmendForeignReliefsBody, ForeignIncomeTaxCreditRelief, ForeignTaxCreditRelief, ForeignTaxForFtcrNotClaimed}

object CreateAndAmendForeignReliefsFixtures {

  val foreignTaxCreditReliefModel: ForeignTaxCreditRelief = ForeignTaxCreditRelief(
    amount = 1000.99
  )

  val foreignTaxCreditReliefJson: JsValue = Json.parse(
    s"""
       |{
       |  "amount": 1000.99
       |}
       |""".stripMargin
  )

  val foreignIncomeTaxCreditReliefModel: ForeignIncomeTaxCreditRelief = ForeignIncomeTaxCreditRelief(
    countryCode = "FRA",
    foreignTaxPaid = Some(1000.99),
    taxableAmount = 2000.99,
    employmentLumpSum = true
  )

  val foreignIncomeTaxCreditReliefJson: JsValue = Json.parse(
    s"""
       |{
       |  "countryCode": "FRA",
       |  "foreignTaxPaid": 1000.99,
       |  "taxableAmount": 2000.99,
       |  "employmentLumpSum": true
       |}
       |""".stripMargin
  )

  val foreignTaxForFtcrNotClaimedModel: ForeignTaxForFtcrNotClaimed = ForeignTaxForFtcrNotClaimed(
    amount = 1000.99
  )

  val foreignTaxForFtcrNotClaimedJson: JsValue = Json.parse(
    s"""
       |{
       |  "amount": 1000.99
       |}
       |""".stripMargin
  )

  val requestBodyModel: CreateAndAmendForeignReliefsBody = CreateAndAmendForeignReliefsBody(
    foreignTaxCreditRelief = Some(foreignTaxCreditReliefModel),
    foreignIncomeTaxCreditRelief = Some(Seq(foreignIncomeTaxCreditReliefModel)),
    foreignTaxForFtcrNotClaimed = Some(foreignTaxForFtcrNotClaimedModel)
  )

  val requestBodyJson: JsValue = Json.parse(
    s"""|
        |{
        |  "foreignTaxCreditRelief": $foreignTaxCreditReliefJson,
        |  "foreignIncomeTaxCreditRelief": [$foreignIncomeTaxCreditReliefJson],
        |  "foreignTaxForFtcrNotClaimed": $foreignTaxForFtcrNotClaimedJson
        |}
        |""".stripMargin
  )

  def responseWithHateoasLinks(taxYear: String): JsValue = Json.parse(s"""
       |{
       |  "links": [
       |    {
       |      "href": "/individuals/reliefs/foreign/AA123456A/$taxYear",
       |      "method": "GET",
       |      "rel": "self"
       |    },
       |    {
       |      "href": "/individuals/reliefs/foreign/AA123456A/$taxYear",
       |      "method": "PUT",
       |      "rel": "create-and-amend-reliefs-foreign"
       |    },
       |    {
       |      "href": "/individuals/reliefs/foreign/AA123456A/$taxYear",
       |      "method": "DELETE",
       |      "rel": "delete-reliefs-foreign"
       |    }
       |  ]
       |}
       |""".stripMargin)

}
