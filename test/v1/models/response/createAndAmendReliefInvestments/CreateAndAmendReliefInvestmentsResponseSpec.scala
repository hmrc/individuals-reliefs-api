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

package v1.models.response.createAndAmendReliefInvestments

import api.models.hateoas
import api.models.hateoas.Method.{DELETE, GET, PUT}
import mocks.MockAppConfig
import support.UnitSpec

class CreateAndAmendReliefInvestmentsResponseSpec extends UnitSpec with MockAppConfig {

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"

      MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes
      CreateAndAmendReliefInvestmentsResponse.LinksFactory.links(mockAppConfig, CreateAndAmendReliefInvestmentsHateoasData(nino, taxYear)) shouldBe
        Seq(
          hateoas.Link(s"/my/context/investment/$nino/$taxYear", GET, "self"),
          hateoas.Link(s"/my/context/investment/$nino/$taxYear", PUT, "create-and-amend-reliefs-investments"),
          hateoas.Link(s"/my/context/investment/$nino/$taxYear", DELETE, "delete-reliefs-investments")
        )
    }
  }

}
