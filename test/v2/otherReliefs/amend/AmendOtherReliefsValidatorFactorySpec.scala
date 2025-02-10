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

package v2.otherReliefs.amend

import play.api.libs.json.JsObject
import shared.controllers.validators.Validator
import shared.utils.UnitSpec
import v2.otherReliefs.amend.def1.Def1_AmendOtherReliefsValidator
import v2.otherReliefs.amend.model.request.AmendOtherReliefsRequestData

class AmendOtherReliefsValidatorFactorySpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validTaxYear = "22-2023"
  private val emptyBody = JsObject.empty
  private val invalidTaxYear = "78921"

  private val validatorFactory = new AmendOtherReliefsValidatorFactory

  "validator" should {
    "return the Def1 validator" when {
      "given a valid request" in {
        val result: Validator[AmendOtherReliefsRequestData] = validatorFactory.validator(validNino, validTaxYear, emptyBody)
        result shouldBe a[Def1_AmendOtherReliefsValidator]

      }

      "given a valid taxYear" in {
        val result: Validator[AmendOtherReliefsRequestData] = validatorFactory.validator(validNino, invalidTaxYear, emptyBody)
        result shouldBe a[Def1_AmendOtherReliefsValidator]

      }
    }

  }
}
