/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.pensionReliefs.createAmend.def1.model.request

import play.api.libs.json.*
import shared.utils.UnitSpec

class CreateAmendPensionsReliefsBodySpec extends UnitSpec {

  val json: JsValue = Json.parse("""
     |{
     |  "pensionReliefs": {
     |    "regularPensionContributions": 1999.99,
     |    "oneOffPensionContributionsPaid": 1999.99,
     |    "retirementAnnuityPayments": 1999.99,
     |    "paymentToEmployersSchemeNoTaxRelief": 1999.99,
     |    "overseasPensionSchemeContributions": 1999.99
     |  }
     |}""".stripMargin)

  val model: CreateAmendPensionsReliefsBody = CreateAmendPensionsReliefsBody(
    pensionReliefs = PensionReliefs(
      regularPensionContributions = Some(1999.99),
      oneOffPensionContributionsPaid = Some(1999.99),
      retirementAnnuityPayments = Some(1999.99),
      paymentToEmployersSchemeNoTaxRelief = Some(1999.99),
      overseasPensionSchemeContributions = Some(1999.99)
    )
  )

  "reads" should {
    "read JSON to a model" in {
      json.as[CreateAmendPensionsReliefsBody] shouldBe model
    }
  }

  "writes" should {
    "write a model to JSON" in {
      Json.toJson(model) shouldBe json
    }
  }

  "isIncorrectOrEmptyBody" should {
    "return true" when {
      "pensionReliefs is empty" in {
        val pensionReliefs = PensionReliefs(None, None, None, None, None)
        CreateAmendPensionsReliefsBody(pensionReliefs).isIncorrectOrEmptyBody shouldBe true
      }
      "pensionReliefs is not empty" in {
        val pensionReliefs = PensionReliefs(Some(1), None, None, None, None)
        CreateAmendPensionsReliefsBody(pensionReliefs).isIncorrectOrEmptyBody shouldBe false
      }
    }
  }

  "error when JSON is invalid" in {
    val invalidJson = Json.obj(
      "pensionReliefs" -> Json.arr()
    )
    invalidJson.validate[CreateAmendPensionsReliefsBody] shouldBe a[JsError]
  }

}
