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

package v1.retrieveCharitableGivingReliefs

import api.controllers.validators.resolvers.ResolveTaxYear
import api.models.domain.TaxYear
import play.api.libs.json.Reads
import v1.retrieveCharitableGivingReliefs.def1.model.response.Def1_RetrieveCharitableGivingReliefsResponse
import v1.retrieveCharitableGivingReliefs.def2.model.response.Def2_RetrieveCharitableGivingReliefsResponse

import scala.math.Ordered.orderingToOrdered

sealed trait RetrieveCharitableGivingReliefsSchema

object RetrieveCharitableGivingReliefsSchema {

  case object Def1 extends RetrieveCharitableGivingReliefsSchema {
    type DownstreamResp = Def1_RetrieveCharitableGivingReliefsResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrieveCharitableGivingReliefsResponse.reads
  }

  case object Def2 extends RetrieveCharitableGivingReliefsSchema {
    type DownstreamResp = Def2_RetrieveCharitableGivingReliefsResponse
    val connectorReads: Reads[DownstreamResp] = Def2_RetrieveCharitableGivingReliefsResponse.reads
  }

  private val defaultSchema = Def1

  def schemaFor(maybeTaxYear: Option[String], ifsEnabled: Boolean): RetrieveCharitableGivingReliefsSchema = {
    maybeTaxYear
      .map(ResolveTaxYear.apply)
      .flatMap(_.toOption.map(schemaFor(_, ifsEnabled)))
      .getOrElse(defaultSchema)
  }

  def schemaFor(taxYear: TaxYear, ifsEnabled: Boolean): RetrieveCharitableGivingReliefsSchema = {
    if (taxYear <= TaxYear.starting(2023) && ifsEnabled == false) Def1
    else defaultSchema
  }

}
