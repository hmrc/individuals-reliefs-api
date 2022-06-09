/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.response.retrieveCharitableGivingTaxRelief

import mocks.MockAppConfig
import play.api.libs.json.Json
import support.UnitSpec
import v1.models.hateoas.Link
import v1.models.hateoas.Method.{DELETE, GET, PUT}

class RetrieveCharitableGivingReliefResponseSpec extends UnitSpec with MockAppConfig with RetrieveCharitableGivingReliefFixture {

  "RetrieveCharitableGivingReliefResponse reads" must {
    "read from downstream JSON" in {
      charitableGivingReliefResponseDownstreamJson.as[RetrieveCharitableGivingReliefResponse] shouldBe charitableGivingReliefResponse
    }
  }

  "RetrieveCharitableGivingReliefResponse writes" must {
    "write to MTD JSON" in {
      Json.toJson(charitableGivingReliefResponse) shouldBe charitableGivingReliefResponseMtdJson
    }
  }

  "LinksFactory" should {
    "return the correct links" in {
      val nino    = "mynino"
      val taxYear = "mytaxyear"
      val context = "individuals/reliefs"

      MockAppConfig.apiGatewayContext.returns(context).anyNumberOfTimes
      RetrieveCharitableGivingReliefResponse.LinksFactory.links(mockAppConfig, RetrieveCharitableGivingReliefHateoasData(nino, taxYear)) shouldBe
        Seq(
          Link(s"/$context/charitable-giving/$nino/$taxYear", PUT, "create-and-amend-charitable-giving-tax-relief"),
          Link(s"/$context/charitable-giving/$nino/$taxYear", GET, "self"),
          Link(s"/$context/charitable-giving/$nino/$taxYear", DELETE, "delete-charitable-giving-tax-relief")
        )
    }
  }

}
