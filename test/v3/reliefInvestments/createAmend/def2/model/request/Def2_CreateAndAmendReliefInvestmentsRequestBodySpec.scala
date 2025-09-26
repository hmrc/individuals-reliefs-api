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

package v3.reliefInvestments.createAmend.def2.model.request

import play.api.libs.json.*
import shared.utils.UnitSpec
import v3.reliefInvestments.createAmend.def2.model.Def2_CreateAndAmendReliefInvestmentsFixtures.*

class Def2_CreateAndAmendReliefInvestmentsRequestBodySpec extends UnitSpec {

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        Def2_requestBodyJson.as[Def2_CreateAndAmendReliefInvestmentsRequestBody] shouldBe Def2_requestBodyModel
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid json" in {
        Json.toJson(Def2_requestBodyModel) shouldBe Def2_requestBodyJson
      }
    }
  }

  "isIncorrectOrEmptyBodyError" should {
    "return false" when {
      "all arrays are provided, none are empty, no objects in the arrays are empty" in {
        Def2_requestBodyModel.isIncorrectOrEmptyBody shouldBe false
      }

      "only some arrays are provided, none are empty, no objects in the arrays are empty" in {
        val model = Def2_requestBodyModel.copy(
          vctSubscription = None,
          seedEnterpriseInvestment = None
        )
        model.isIncorrectOrEmptyBody shouldBe false
      }
    }

    "return true" when {
      "no arrays are provided" in {
        val model = Def2_CreateAndAmendReliefInvestmentsRequestBody(
          None,
          None,
          None,
          None
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
      "at least one empty array is provided" in {
        val model = Def2_requestBodyModel.copy(
          vctSubscription = Some(Seq()),
          communityInvestment = Some(Seq())
        )
        model.isIncorrectOrEmptyBody shouldBe true
      }
    }
  }

  "error when JSON is invalid" in {
    val invalidJson = Json.obj(
      "vctSubscription" -> "not-an-array"
    )
    invalidJson.validate[Def2_CreateAndAmendReliefInvestmentsRequestBody] shouldBe a[JsError]
  }

}
