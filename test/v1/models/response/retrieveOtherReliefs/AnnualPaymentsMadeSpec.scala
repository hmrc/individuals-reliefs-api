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

package v1.models.response.retrieveOtherReliefs

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class AnnualPaymentsMadeSpec extends UnitSpec with JsonErrorValidators {
  val annualPaymentsMade: AnnualPaymentsMade = AnnualPaymentsMade(Some("myRef"), 763.00)

  val noRefAnnualPaymentsMade: AnnualPaymentsMade = AnnualPaymentsMade(None, 763.00)

  val json = Json.parse(
    """{
      |        "customerReference": "myRef",
      |        "reliefClaimed": 763.00
      |      }""".stripMargin
  )

  val noRefJson = Json.parse(
    """{
      |        "reliefClaimed": 763.00
      |      }""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        annualPaymentsMade shouldBe json.as[AnnualPaymentsMade]
      }
    }
  }

  "reads from a JSON with no reference" when {
    "passed a JSON with no customer reference" should {
      "return a model with no customer reference " in {
        noRefAnnualPaymentsMade shouldBe noRefJson.as[AnnualPaymentsMade]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(annualPaymentsMade) shouldBe json
      }
    }
  }

  "writes from a model with no reference" when {
    "passed a model with no customer reference" should {
      "return a JSON with no customer reference" in {
        Json.toJson(noRefAnnualPaymentsMade) shouldBe noRefJson
      }
    }
  }

}
