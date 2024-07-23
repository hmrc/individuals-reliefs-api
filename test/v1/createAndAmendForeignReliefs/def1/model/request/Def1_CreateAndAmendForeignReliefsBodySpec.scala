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

package v1.createAndAmendForeignReliefs.def1.model.request

import api.models.utils.JsonErrorValidators
import play.api.libs.json.Json
import support.UnitSpec
import v1.createAndAmendForeignReliefs.CreateAndAmendForeignReliefsFixtures.{requestBodyJson, requestBodyModel}

class Def1_CreateAndAmendForeignReliefsBodySpec extends UnitSpec with JsonErrorValidators {

  private val emptyCreateAndAmendForeignReliefsBody = Def1_CreateAndAmendForeignReliefsBody(
    None,
    None,
    None
  )

  private val emptyJson = Json.parse("""{}""")

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        requestBodyModel shouldBe requestBodyJson.as[Def1_CreateAndAmendForeignReliefsBody]
      }
    }
  }

  "read from empty JSON" should {
    "convert empty MTD JSON into an empty CreateAndAmendForeignReliefsBody object" in {
      emptyCreateAndAmendForeignReliefsBody shouldBe emptyJson.as[Def1_CreateAndAmendForeignReliefsBody]
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(requestBodyModel) shouldBe requestBodyJson
      }
    }
  }

  "write from an empty body" when {
    "passed an empty model" should {
      "return an empty JSON" in {
        Json.toJson(emptyCreateAndAmendForeignReliefsBody) shouldBe emptyJson
      }
    }
  }

}
