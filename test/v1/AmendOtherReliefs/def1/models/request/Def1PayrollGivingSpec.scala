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

package v1.AmendOtherReliefs.def1.models.request

import api.models.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec
import v1.AmendOtherReliefs.def1.model.request.Def1_PayrollGiving

class Def1PayrollGivingSpec extends UnitSpec with JsonErrorValidators {

  val payrollGiving = Def1_PayrollGiving(
    Some("myRef"),
    154.00
  )

  val noRefPayrollGiving = Def1_PayrollGiving(
    None,
    154.00
  )

  val json = Json.parse(
    """{
      |        "customerReference": "myRef",
      |        "reliefClaimed": 154.00
      |      }""".stripMargin
  )

  val noRefJson = Json.parse(
    """{
      |        "reliefClaimed": 154.00
      |      }""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        payrollGiving shouldBe json.as[Def1_PayrollGiving]
      }
    }
  }

  "reads from a JSON with no reference" when {
    "passed a JSON with no customer reference" should {
      "return a model with no customer reference " in {
        noRefPayrollGiving shouldBe noRefJson.as[Def1_PayrollGiving]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(payrollGiving) shouldBe json
      }
    }
  }

  "writes from a model with no reference" when {
    "passed a model with no customer reference" should {
      "return a JSON with no customer reference" in {
        Json.toJson(noRefPayrollGiving) shouldBe noRefJson
      }
    }
  }

}
