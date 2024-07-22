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

package v1.otherReliefs.retrieve

import api.schema.DownstreamReadable
import play.api.libs.json.Reads
import v1.otherReliefs.retrieve.def1.model.response.Def1_RetrieveOtherReliefsResponse
import v1.otherReliefs.retrieve.model.response.RetrieveOtherReliefsResponse

sealed trait RetrieveOtherReliefsSchema extends DownstreamReadable[RetrieveOtherReliefsResponse]

object RetrieveOtherReliefsSchema {

  case object Def1 extends RetrieveOtherReliefsSchema {
    type DownstreamResp = Def1_RetrieveOtherReliefsResponse
    val connectorReads: Reads[DownstreamResp] = Def1_RetrieveOtherReliefsResponse.reads
  }

  val schema: RetrieveOtherReliefsSchema = Def1

}
