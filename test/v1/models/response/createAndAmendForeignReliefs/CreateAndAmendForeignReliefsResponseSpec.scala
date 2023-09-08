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

package v1.models.response.createAndAmendForeignReliefs

import api.hateoas.Link
import api.hateoas.Method._
import mocks.MockAppConfig
import support.UnitSpec

class CreateAndAmendForeignReliefsResponseSpec extends UnitSpec with MockAppConfig {

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"

      MockAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()
      CreateAndAmendForeignReliefsResponse.LinksFactory.links(mockAppConfig, CreateAndAmendForeignReliefsHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/my/context/foreign/$nino/$taxYear", GET, "self"),
          api.hateoas.Link(s"/my/context/foreign/$nino/$taxYear", PUT, "create-and-amend-reliefs-foreign"),
          api.hateoas.Link(s"/my/context/foreign/$nino/$taxYear", DELETE, "delete-reliefs-foreign")
        )
    }
  }

}
