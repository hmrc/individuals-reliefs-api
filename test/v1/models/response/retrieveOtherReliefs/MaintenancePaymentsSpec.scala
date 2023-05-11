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

package v1.models.response.retrieveOtherReliefs

import api.models.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec

class MaintenancePaymentsSpec extends UnitSpec with JsonErrorValidators {
  val maintenancePayments: MaintenancePayments = MaintenancePayments(Some("myRef"), Some("Hilda"), Some("2000-01-01"), 222.22)

  val noOptionsMaintenancePayments: MaintenancePayments = MaintenancePayments(
    None,
    None,
    None,
    222.22
  )

  val json = Json.parse("""
      |{
      |  "customerReference": "myRef",
      |  "exSpouseName" : "Hilda",
      |  "exSpouseDateOfBirth": "2000-01-01",
      |  "amount": 222.22
      |}
      |""".stripMargin)

  val noOptionsJson = Json.parse("""
      |{
      |  "amount": 222.22
      |}
      |""".stripMargin)

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        maintenancePayments shouldBe json.as[MaintenancePayments]
      }
    }
  }

  "reads from a json with no optional fields supplied" when {
    "passed a JSON with no optional fields" should {
      "return a model with no optional fields" in {
        noOptionsMaintenancePayments shouldBe noOptionsJson.as[MaintenancePayments]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(maintenancePayments) shouldBe json
      }
    }
  }

  "writes from a model with no optional fields" when {
    "passed a model with no optional fields" should {
      "return a json with no optional fields" in {
        Json.toJson(noOptionsMaintenancePayments) shouldBe noOptionsJson
      }
    }
  }

}
