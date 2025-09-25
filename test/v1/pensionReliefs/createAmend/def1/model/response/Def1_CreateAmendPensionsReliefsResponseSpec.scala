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

package v1.pensionReliefs.createAmend.def1.model.response

import play.api.libs.json.Json
import shared.config.MockSharedAppConfig
import shared.hateoas.Link
import shared.hateoas.Method.*
import shared.utils.UnitSpec
import v1.pensionReliefs.createAmend.model.response.{CreateAmendPensionsReliefsHateoasData, CreateAmendPensionsReliefsResponse}

class Def1_CreateAmendPensionsReliefsResponseSpec extends UnitSpec with MockSharedAppConfig {

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        val model  = new Def1_CreateAmendPensionsReliefsResponse {}
        val result = Json.toJson(model)(Def1_CreateAmendPensionsReliefsResponse.writes)
        result shouldBe Json.obj()
      }
    }
  }

  "CreateAmendPensionsReliefsResponse writes" when {
    "passed Def1_CreateAmendPensionsReliefsResponse" should {
      "return valid JSON using parent trait writes" in {
        val model: CreateAmendPensionsReliefsResponse = new Def1_CreateAmendPensionsReliefsResponse {}
        val result                                    = Json.toJson(model)(CreateAmendPensionsReliefsResponse.writes)
        result shouldBe Json.obj()
      }
    }
  }

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        Json.obj().as[Def1_CreateAmendPensionsReliefsResponse] shouldBe a[Def1_CreateAmendPensionsReliefsResponse]
      }
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"

      MockedSharedAppConfig.apiGatewayContext.returns("my/context").anyNumberOfTimes()
      CreateAmendPensionsReliefsResponse.LinksFactory.links(mockSharedAppConfig, CreateAmendPensionsReliefsHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/my/context/pensions/$nino/$taxYear", GET, "self"),
          Link(s"/my/context/pensions/$nino/$taxYear", PUT, "create-and-amend-reliefs-pensions"),
          Link(s"/my/context/pensions/$nino/$taxYear", DELETE, "delete-reliefs-pensions")
        )
    }
  }

}
