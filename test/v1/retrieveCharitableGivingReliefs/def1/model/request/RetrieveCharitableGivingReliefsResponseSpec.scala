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

package v1.retrieveCharitableGivingReliefs.def1.model.request

import api.hateoas.Link
import api.hateoas.Method._
import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.retrieveCharitableGivingReliefs.model.response.{
  Def1_RetrieveCharitableGivingReliefsResponse,
  RetrieveCharitableGivingReliefsHateoasData,
  RetrieveCharitableGivingReliefsResponse
}

class RetrieveCharitableGivingReliefsResponseSpec extends UnitSpec with MockAppConfig with RetrieveCharitableGivingReliefsFixture {

  "RetrieveCharitableGivingReliefResponse reads" must {
    "read from downstream JSON" in {
      charitableGivingReliefsResponseDownstreamJson.as[Def1_RetrieveCharitableGivingReliefsResponse] shouldBe charitableGivingReliefsResponse
    }
  }

  "RetrieveCharitableGivingReliefResponse writes" must {
    "write to MTD JSON" in {
      Json.toJson(charitableGivingReliefsResponse) shouldBe charitableGivingReliefsResponseMtdJson
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"
      val context = "individuals/reliefs"

      MockedAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes()
      RetrieveCharitableGivingReliefsResponse.LinksFactory.links(mockAppConfig, RetrieveCharitableGivingReliefsHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/$context/charitable-giving/$nino/$taxYear", PUT, "create-and-amend-charitable-giving-tax-relief"),
          api.hateoas.Link(s"/$context/charitable-giving/$nino/$taxYear", GET, "self"),
          api.hateoas.Link(s"/$context/charitable-giving/$nino/$taxYear", DELETE, "delete-charitable-giving-tax-relief")
        )
    }
  }

}
