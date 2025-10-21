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

package v1.retrieveCharitableGivingReliefs

import cats.data.Validated
import cats.data.Validated.Valid
import play.api.libs.json.Reads
import shared.controllers.validators.resolvers.ResolveTaxYearMinimum
import shared.models.domain.TaxYear
import shared.models.errors.MtdError
import shared.schema.DownstreamReadable
import v1.retrieveCharitableGivingReliefs.model.response.*
import scala.math.Ordering.Implicits.infixOrderingOps

sealed trait RetrieveCharitableGivingReliefsSchema extends DownstreamReadable[RetrieveCharitableGivingReliefsResponse]

object RetrieveCharitableGivingReliefsSchema {

  case object Def1 extends RetrieveCharitableGivingReliefsSchema {
    type DownstreamResp = Def1_RetrieveCharitableGivingReliefsResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrieveCharitableGivingReliefsResponse.format.reads(_)
  }

  case object Def2 extends RetrieveCharitableGivingReliefsSchema {
    type DownstreamResp = Def2_RetrieveCharitableGivingReliefsResponse
    val connectorReads: Reads[DownstreamResp] = Def2_RetrieveCharitableGivingReliefsResponse.format.reads(_)
  }

  def schemaFor(taxYearString: String): Validated[Seq[MtdError], RetrieveCharitableGivingReliefsSchema] =
    ResolveTaxYearMinimum(TaxYear.ending(2018))(taxYearString) andThen schemaFor

  def schemaFor(taxYear: TaxYear): Validated[Seq[MtdError], RetrieveCharitableGivingReliefsSchema] = {
    if (taxYear >= TaxYear.fromMtd("2025-26")) Valid(Def2) else Valid(Def1)
  }

}
