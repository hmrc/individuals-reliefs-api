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

package v1.pensionReliefs.createAmend.def1.model.request

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class PensionReliefsSpec extends UnitSpec {

  val maxJson: JsValue = Json.parse("""
      |{
      |  "regularPensionContributions": 1999.99,
      |  "oneOffPensionContributionsPaid": 1999.99,
      |  "retirementAnnuityPayments": 1999.99,
      |  "paymentToEmployersSchemeNoTaxRelief": 1999.99,
      |  "overseasPensionSchemeContributions": 1999.99
      |}""".stripMargin)

  val maxModel: PensionReliefs = PensionReliefs(
    regularPensionContributions = Some(1999.99),
    oneOffPensionContributionsPaid = Some(1999.99),
    retirementAnnuityPayments = Some(1999.99),
    paymentToEmployersSchemeNoTaxRelief = Some(1999.99),
    overseasPensionSchemeContributions = Some(1999.99)
  )

  val minJson: JsValue = Json.parse("""
      |{}""".stripMargin)

  val minModel: PensionReliefs = PensionReliefs(
    regularPensionContributions = None,
    oneOffPensionContributionsPaid = None,
    retirementAnnuityPayments = None,
    paymentToEmployersSchemeNoTaxRelief = None,
    overseasPensionSchemeContributions = None
  )

  "reads" should {
    "read JSON to a model" when {
      "passed max fields" in {
        maxJson.as[PensionReliefs] shouldBe maxModel
      }
      "passed min fields" in {
        minJson.as[PensionReliefs] shouldBe minModel
      }
    }
  }

  "writes" should {
    "write a model to JSON" when {
      "passed max fields" in {
        Json.toJson(maxModel) shouldBe maxJson
      }
      "passed min fields" in {
        Json.toJson(minModel) shouldBe minJson
      }
    }
  }

  "isEmpty" should {
    "return true" when {
      "all fields are empty" in {
        minModel.isEmpty shouldBe true
      }
    }
    "return false" when {
      "regularPensionContributions is not empty" in {
        minModel.copy(regularPensionContributions = Some(1)).isEmpty shouldBe false
      }
      "oneOffPensionContributionsPaid is not empty" in {
        minModel.copy(oneOffPensionContributionsPaid = Some(1)).isEmpty shouldBe false
      }
      "retirementAnnuityPayments is not empty" in {
        minModel.copy(retirementAnnuityPayments = Some(1)).isEmpty shouldBe false
      }
      "paymentToEmployersSchemeNoTaxRelief is not empty" in {
        minModel.copy(paymentToEmployersSchemeNoTaxRelief = Some(1)).isEmpty shouldBe false
      }
      "overseasPensionSchemeContributions is not empty" in {
        minModel.copy(overseasPensionSchemeContributions = Some(1)).isEmpty shouldBe false
      }
    }
  }

}
