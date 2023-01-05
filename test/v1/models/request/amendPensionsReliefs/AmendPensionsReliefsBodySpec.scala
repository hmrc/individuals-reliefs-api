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

package v1.models.request.amendPensionsReliefs

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class AmendPensionsReliefsBodySpec extends UnitSpec {

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

  val model: AmendPensionsReliefsBody = AmendPensionsReliefsBody(
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
      json.as[AmendPensionsReliefsBody] shouldBe model
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
        AmendPensionsReliefsBody(pensionReliefs).isIncorrectOrEmptyBody shouldBe true
      }
      "pensionReliefs is not empty" in {
        val pensionReliefs = PensionReliefs(Some(1), None, None, None, None)
        AmendPensionsReliefsBody(pensionReliefs).isIncorrectOrEmptyBody shouldBe false
      }
    }
  }

}
