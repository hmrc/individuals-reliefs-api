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

package v3.pensionReliefs.retrieve

import play.api.libs.json.Reads
import shared.schema.DownstreamReadable
import v3.pensionReliefs.retrieve.def1.model.response.Def1_RetrievePensionsReliefsResponse
import v3.pensionReliefs.retrieve.model.response.RetrievePensionsReliefsResponse

sealed trait RetrievePensionsReliefsSchema extends DownstreamReadable[RetrievePensionsReliefsResponse]

object RetrievePensionsReliefsSchema {

  case object Def1 extends RetrievePensionsReliefsSchema {
    type DownstreamResp = Def1_RetrievePensionsReliefsResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrievePensionsReliefsResponse.reads
  }

  val schema: RetrievePensionsReliefsSchema = Def1

}
