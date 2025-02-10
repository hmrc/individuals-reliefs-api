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

package v2.reliefInvestments.retrieve.def1.model.response

import play.api.libs.json.Json
import shared.config.MockSharedAppConfig
import shared.hateoas.Link
import shared.hateoas.Method._
import shared.utils.UnitSpec
import v2.fixtures.RetrieveReliefInvestmentsFixtures.{responseJson, responseModel}
import v2.reliefInvestments.retrieve.model.response._

class Def1_RetrieveReliefInvestmentsResponseSpec extends UnitSpec with MockSharedAppConfig {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        responseJson.as[Def1_RetrieveReliefInvestmentsResponse] shouldBe responseModel
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(responseModel) shouldBe responseJson
      }
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"

      MockedSharedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()
      RetrieveReliefInvestmentsResponse.LinksFactory.links(mockSharedAppConfig, RetrieveReliefInvestmentsHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/my/context/investment/$nino/$taxYear", GET, "self"),
          Link(s"/my/context/investment/$nino/$taxYear", PUT, "create-and-amend-reliefs-investments"),
          Link(s"/my/context/investment/$nino/$taxYear", DELETE, "delete-reliefs-investments")
        )
    }
  }

}
