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

package v1.reliefInvestments.retrieve

import shared.controllers.validators.Validator
import v1.reliefInvestments.retrieve.RetrieveReliefInvestmentsSchema.Def1
import v1.reliefInvestments.retrieve.def1.model.Def1_RetrieveReliefInvestmentsValidator
import v1.reliefInvestments.retrieve.model.request.RetrieveReliefInvestmentsRequestData

import javax.inject.Singleton

@Singleton
class RetrieveReliefInvestmentsValidatorFactory {

  def validator(nino: String, taxYear: String): Validator[RetrieveReliefInvestmentsRequestData] = {

    val schema = RetrieveReliefInvestmentsSchema.schemaFor(Some(taxYear))

    schema match {
      case Def1 => new Def1_RetrieveReliefInvestmentsValidator(nino, taxYear)
    }

  }

}
