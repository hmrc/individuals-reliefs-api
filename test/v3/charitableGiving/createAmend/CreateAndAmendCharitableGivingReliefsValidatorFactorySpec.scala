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

package v3.charitableGiving.createAmend

import play.api.libs.json.*
import shared.controllers.validators.AlwaysErrorsValidator
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v3.charitableGiving.createAmend.def1.Def1_CreateAndAmendCharitableGivingReliefsValidator
import v3.charitableGiving.createAmend.def2.Def2_CreateAndAmendCharitableGivingReliefsValidator

class CreateAndAmendCharitableGivingReliefsValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validNino = "AA123456A"

  val requestBodyJson: JsValue = Json.parse(
    s"""
       |{
       |
       |}
     """.stripMargin
  )

  private def validatorFactory(taxYear: String) =
    new CreateAndAmendCharitableGivingReliefsValidatorFactory().validator(nino = validNino, taxYear = taxYear, body = requestBodyJson)

  "running a validation" should {
    "return the Def1 validator" when {
      "given a request handled by a Def1 schema" in {
        validatorFactory("2023-24") shouldBe a[Def1_CreateAndAmendCharitableGivingReliefsValidator]
      }
    }

    "return the Def2 validator" when {
      "given a request handled by a Def2 schema" in {
        validatorFactory("2025-26") shouldBe a[Def2_CreateAndAmendCharitableGivingReliefsValidator]
      }
    }

    "return no valid schema" when {
      "given a request handled by no valid schema" in {
        validatorFactory("INVALID_TAX_YEAR") shouldBe a[AlwaysErrorsValidator]
      }
    }
  }

}
