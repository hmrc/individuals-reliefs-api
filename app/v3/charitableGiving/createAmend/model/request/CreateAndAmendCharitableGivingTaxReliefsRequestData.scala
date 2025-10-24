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

package v3.charitableGiving.createAmend.model.request

import shared.models.domain.{Nino, TaxYear}
import v3.charitableGiving.createAmend.def1.model.request.Def1_CreateAndAmendCharitableGivingTaxReliefsBody
import v3.charitableGiving.createAmend.def2.model.request.Def2_CreateAndAmendCharitableGivingTaxReliefsBody

sealed trait CreateAndAmendCharitableGivingTaxReliefsRequestData {
  val nino: Nino
  val taxYear: TaxYear
  val body: CreateAndAmendCharitableGivingTaxReliefsBody
}

case class Def1_CreateAndAmendCharitableGivingTaxReliefsRequestData(nino: Nino,
                                                                    taxYear: TaxYear,
                                                                    body: Def1_CreateAndAmendCharitableGivingTaxReliefsBody)
    extends CreateAndAmendCharitableGivingTaxReliefsRequestData

case class Def2_CreateAndAmendCharitableGivingTaxReliefsRequestData(nino: Nino,
                                                                    taxYear: TaxYear,
                                                                    body: Def2_CreateAndAmendCharitableGivingTaxReliefsBody)
    extends CreateAndAmendCharitableGivingTaxReliefsRequestData
