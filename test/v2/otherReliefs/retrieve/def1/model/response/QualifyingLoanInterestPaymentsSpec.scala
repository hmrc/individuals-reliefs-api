/*
 * Copyright 2024 HM Revenue & Customs
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

package v2.otherReliefs.retrieve.def1.model.response

import play.api.libs.json.Json
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec

class QualifyingLoanInterestPaymentsSpec extends UnitSpec with JsonErrorValidators {

  val qualifyingLoanInterestPayments: QualifyingLoanInterestPayments = QualifyingLoanInterestPayments(
    Some("myRef"),
    Some("Maurice"),
    763.00
  )

  val noOptionsQualifyingLoanInterestPayments: QualifyingLoanInterestPayments = QualifyingLoanInterestPayments(
    None,
    None,
    763.00
  )

  val json = Json.parse(
    """{
      |  "customerReference": "myRef",
      |  "lenderName": "Maurice",
      |  "reliefClaimed": 763.00
      |}""".stripMargin
  )

  val noOptionsJson = Json.parse(
    """{
      |  "reliefClaimed": 763.00
      |}""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        qualifyingLoanInterestPayments shouldBe json.as[QualifyingLoanInterestPayments]
      }
    }
  }

  "reads from a JSON with no lender name" when {
    "passed a JSON with no customer lender name" should {
      "return a model with no customer lender name" in {
        noOptionsQualifyingLoanInterestPayments shouldBe noOptionsJson.as[QualifyingLoanInterestPayments]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(qualifyingLoanInterestPayments) shouldBe json
      }
    }
  }

  "writes from a model with no lender name" when {
    "passed a model with no customer lender name" should {
      "return a JSON with no customer lender name" in {
        Json.toJson(noOptionsQualifyingLoanInterestPayments) shouldBe noOptionsJson
      }
    }
  }

}
