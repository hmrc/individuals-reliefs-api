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

package v1.otherReliefs.retrieve

import shared.controllers.validators.Validator
import v1.otherReliefs.retrieve.RetrieveOtherReliefsSchema.Def1
import v1.otherReliefs.retrieve.def1.model.Def1_RetrieveOtherReliefsValidator
import v1.otherReliefs.retrieve.model.request.RetrieveOtherReliefsRequestData

import javax.inject.Singleton

@Singleton
class RetrieveOtherReliefsValidatorFactory {
  def validator(nino: String, taxYear: String): Validator[RetrieveOtherReliefsRequestData] = {

    val schema = RetrieveOtherReliefsSchema.schemaFor(Some(taxYear))

    schema match {
      case Def1 => new Def1_RetrieveOtherReliefsValidator(nino, taxYear)
    }

  }

}
