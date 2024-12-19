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

package v1.otherReliefs.amend.model.response

import shared.config.MockSharedAppConfig
import shared.hateoas.Link
import shared.hateoas.Method._
import shared.utils.UnitSpec

class AmendOtherReliefsResponseSpec extends UnitSpec with MockSharedAppConfig {

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"

      MockedSharedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()
      AmendOtherReliefsResponse.LinksFactory.links(mockSharedAppConfig, AmendOtherReliefsHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/my/context/other/$nino/$taxYear", PUT, "create-and-amend-reliefs-other"),
          Link(s"/my/context/other/$nino/$taxYear", GET, "self"),
          Link(s"/my/context/other/$nino/$taxYear", DELETE, "delete-reliefs-other")
        )
    }
  }

}
