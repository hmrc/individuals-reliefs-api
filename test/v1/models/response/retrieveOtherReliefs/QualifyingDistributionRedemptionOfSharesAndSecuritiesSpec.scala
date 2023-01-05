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

import play.api.libs.json.Json
import support.UnitSpec
import v1.models.utils.JsonErrorValidators

class QualifyingDistributionRedemptionOfSharesAndSecuritiesSpec extends UnitSpec with JsonErrorValidators {

  val qualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities =
    QualifyingDistributionRedemptionOfSharesAndSecurities(
      Some("myref"),
      222.22
    )

  val noRefQualifyingDistributionRedemptionOfSharesAndSecurities: QualifyingDistributionRedemptionOfSharesAndSecurities =
    QualifyingDistributionRedemptionOfSharesAndSecurities(
      None,
      222.22
    )

  val json = Json.parse(
    """{
      |        "customerReference": "myref",
      |        "amount": 222.22
      |      }""".stripMargin
  )

  val noRefJson = Json.parse(
    """{
      |        "amount": 222.22
      |      }""".stripMargin
  )

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        qualifyingDistributionRedemptionOfSharesAndSecurities shouldBe json.as[QualifyingDistributionRedemptionOfSharesAndSecurities]
      }
    }
  }

  "reads from a JSON with no reference" when {
    "passed a JSON with no customer reference" should {
      "return a model with no customer reference " in {
        noRefQualifyingDistributionRedemptionOfSharesAndSecurities shouldBe noRefJson.as[QualifyingDistributionRedemptionOfSharesAndSecurities]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(qualifyingDistributionRedemptionOfSharesAndSecurities) shouldBe json
      }
    }
  }

  "writes from a model with no reference" when {
    "passed a model with no customer reference" should {
      "return a JSON with no customer reference" in {
        Json.toJson(noRefQualifyingDistributionRedemptionOfSharesAndSecurities) shouldBe noRefJson
      }
    }
  }

}
