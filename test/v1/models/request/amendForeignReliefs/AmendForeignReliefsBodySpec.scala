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

package v1.models.request.amendForeignReliefs

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class AmendForeignReliefsBodySpec extends UnitSpec with JsonErrorValidators {

  private val amount = 1234.56

  private val amendForeignReliefsBody = AmendForeignReliefsBody(
    foreignTaxCreditRelief = Some(ForeignTaxCreditRelief(
      amount = amount
    )),
    foreignIncomeTaxCreditRelief = Some(ForeignIncomeTaxCreditRelief(
      countryCode = "FRA",
      foreignTaxPaid = Some(amount),
      taxableAmount = amount,
      employmentLumpSum = true
    )), foreignTaxForFtcrNotClaimed = Some(ForeignTaxForFtcrNotClaimed(
      amount = amount
    ))
  )

  private val emptyAmendForeignReliefsBody = AmendForeignReliefsBody(
    None, None, None
  )

  private val json = Json.parse(
    s"""|
        |{
        |  "foreignTaxCreditRelief": {
        |    "amount": $amount
        |  },
        |  "foreignIncomeTaxCreditRelief": {
        |    "countryCode": "FRA",
        |    "foreignTaxPaid": $amount,
        |    "taxableAmount": $amount,
        |    "employmentLumpSum": true
        |  },
        |  "foreignTaxForFtcrNotClaimed": {
        |    "amount": $amount
        |  }
        |}
        |""".stripMargin
  )

  private val emptyJson = Json.parse("""{}""")

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        amendForeignReliefsBody shouldBe json.as[AmendForeignReliefsBody]
      }
    }
  }
  "read from empty JSON" should {
    "convert empty MTD JSON into an empty AmendSecuritiesItems object" in {
      emptyAmendForeignReliefsBody shouldBe emptyJson.as[AmendForeignReliefsBody]
    }
  }
  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(amendForeignReliefsBody) shouldBe json
      }
    }
  }
  "write from an empty body" when {
    "passed an empty model" should {
      "return an empty JSON" in {
        Json.toJson(emptyAmendForeignReliefsBody) shouldBe emptyJson
      }
    }
  }




}