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

package v2.charitableGiving.retrieve.def1.model.request

import play.api.libs.json.Json
import shared.config.MockSharedAppConfig
import shared.utils.UnitSpec
import v2.charitableGiving.retrieve.model.response.Def1_RetrieveCharitableGivingReliefsResponse

class RetrieveCharitableGivingReliefsResponseSpec extends UnitSpec with MockSharedAppConfig with RetrieveCharitableGivingReliefsFixture {

  "RetrieveCharitableGivingReliefResponse reads" must {
    "read from downstream (DES) JSON" in {
      charitableGivingReliefsDesResponseDownstreamJson.as[Def1_RetrieveCharitableGivingReliefsResponse] shouldBe charitableGivingReliefsResponse
    }

    "read from downstream (IFS) JSON" in {
      charitableGivingReliefsIfsResponseDownstreamJson.as[Def1_RetrieveCharitableGivingReliefsResponse] shouldBe charitableGivingReliefsResponse
    }
  }

  "RetrieveCharitableGivingReliefResponse writes" must {
    "write to MTD JSON" in {
      Json.toJson(charitableGivingReliefsResponse) shouldBe charitableGivingReliefsResponseMtdJson
    }
  }

}
