/*
 * Copyright 2022 HM Revenue & Customs
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

class ForeignIncomeTaxCreditReliefSpec extends UnitSpec with JsonErrorValidators {

  private val amount = 1234.56

  private val model = ForeignIncomeTaxCreditRelief(
    countryCode = "FRA",
    foreignTaxPaid = Some(amount),
    taxableAmount = amount,
    employmentLumpSum = true
  )

  private val json = Json.parse(
    s"""{
      |  "countryCode": "FRA",
      |  "foreignTaxPaid": $amount,
      |  "taxableAmount": $amount,
      |  "employmentLumpSum": true
      |}""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        json.as[ForeignIncomeTaxCreditRelief] shouldBe model
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(model) shouldBe json
      }
    }
  }

}
