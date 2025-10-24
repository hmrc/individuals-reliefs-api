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

package v3.reliefInvestments.retrieve.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v3.fixtures.retrieveReliefInvestments.{Def1_RetrieveReliefInvestmentsFixtures, Def2_RetrieveReliefInvestmentsFixtures}

class RetrieveReliefInvestmentsResponseSpec extends UnitSpec {

  "RetrieveReliefInvestmentsResponse.writes" should {
    "serialize Def1_RetrieveReliefInvestmentsResponse correctly" in {
      val def1         = Def1_RetrieveReliefInvestmentsFixtures.responseModel
      val expectedJson = Def1_RetrieveReliefInvestmentsFixtures.responseJson

      Json.toJson(def1: RetrieveReliefInvestmentsResponse) shouldBe expectedJson
    }

    "serialize Def2_RetrieveReliefInvestmentsResponse correctly" in {
      val def2         = Def2_RetrieveReliefInvestmentsFixtures.responseModel
      val expectedJson = Def2_RetrieveReliefInvestmentsFixtures.responseJson

      Json.toJson(def2: RetrieveReliefInvestmentsResponse) shouldBe expectedJson
    }
  }

}
