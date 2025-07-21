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

package v3.reliefInvestments.retrieve

import shared.controllers.validators.{AlwaysErrorsValidator, Validator}
import shared.utils.UnitSpec
import v3.reliefInvestments.retrieve.def1.model.Def1_RetrieveReliefInvestmentsValidator
import v3.reliefInvestments.retrieve.def2.model.Def2_RetrieveReliefInvestmentsValidator
import v3.reliefInvestments.retrieve.model.request.RetrieveReliefInvestmentsRequestData

class RetrieveReliefInvestmentsValidatorFactorySpec extends UnitSpec {

  private def validatorFor(taxYear: String): Validator[RetrieveReliefInvestmentsRequestData] =
    new RetrieveReliefInvestmentsValidatorFactory().validator(nino = "ignoredNino", taxYear = taxYear)

  "RetrieveUkPropertyCumulativeSummaryValidatorFactory" when {
    "given a request corresponding to a Def1 schema" should {
      "return a Def1 validator" in {
        validatorFor("2024-25") shouldBe a[Def1_RetrieveReliefInvestmentsValidator]
      }
    }
    "given a request corresponding to a Def2 schema" should {
      "return a Def2 validator" in {
        validatorFor("2025-26") shouldBe a[Def2_RetrieveReliefInvestmentsValidator]
      }
    }

    "given a request where no valid schema could be determined" should {
      "return a validator returning the errors" in {
        validatorFor("BAD_TAX_YEAR") shouldBe an[AlwaysErrorsValidator]
      }
    }
  }

}
