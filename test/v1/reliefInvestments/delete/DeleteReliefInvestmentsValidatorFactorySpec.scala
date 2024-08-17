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

package v1.reliefInvestments.delete

import api.controllers.validators.Validator
import support.UnitSpec
import v1.reliefInvestments.delete.def1.Def1_DeleteReliefInvestmentsValidator
import v1.reliefInvestments.delete.model.DeleteReliefInvestmentsRequestData

class DeleteReliefInvestmentsValidatorFactorySpec extends UnitSpec {

  private val validNino      = "AA123456A"
  private val validTaxYear   = "2022-23"
  private val invalidTaxYear = "2023"

  private val validatorFactory = new DeleteReliefInvestmentsValidatorFactory

  "validator" should {
    "return the Def1 validator" when {
      "given a valid request" in {
        val result: Validator[DeleteReliefInvestmentsRequestData] = validatorFactory.validator(validNino, validTaxYear)
        result shouldBe a[Def1_DeleteReliefInvestmentsValidator]

      }

      "given an invalid taxYear" in {
        val result: Validator[DeleteReliefInvestmentsRequestData] = validatorFactory.validator(validNino, invalidTaxYear)
        result shouldBe a[Def1_DeleteReliefInvestmentsValidator]

      }
    }

  }

}