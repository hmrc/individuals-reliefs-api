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

package v2.foreignReliefs.retrieve.model.response

import play.api.libs.json.Json
import shared.config.MockSharedAppConfig
import shared.models.domain.Timestamp
import shared.utils.UnitSpec
import v2.foreignReliefs.retrieve.def1.model.response.{Def1_ForeignIncomeTaxCreditRelief, Def1_ForeignTaxCreditRelief, Def1_ForeignTaxForFtcrNotClaimed}

class RetrieveForeignReliefsResponseSpec extends UnitSpec with MockSharedAppConfig {

  val retrieveForeignReliefsBody: RetrieveForeignReliefsResponse = Def1_RetrieveForeignReliefsResponse(
    Timestamp("2020-06-17T10:53:38.000Z"),
    Some(Def1_ForeignTaxCreditRelief(763.00)),
    Some(
      Seq(
        Def1_ForeignIncomeTaxCreditRelief(
          "FRA",
          Some(540.32),
          204.78,
          false
        ))),
    Some(Def1_ForeignTaxForFtcrNotClaimed(549.98))
  )

  val json = Json.parse(
    """{
      |  "submittedOn": "2020-06-17T10:53:38.000Z",
      |  "foreignTaxCreditRelief": {
      |    "amount": 763.00
      |  },
      |  "foreignIncomeTaxCreditRelief": [
      |     {
      |      "countryCode": "FRA",
      |      "foreignTaxPaid": 540.32,
      |      "taxableAmount": 204.78,
      |      "employmentLumpSum": false
      |     }
      |  ],
      |  "foreignTaxForFtcrNotClaimed": {
      |      "amount": 549.98
      |  }
      |}""".stripMargin
  )

  val emptyJson = Json.parse("""{}""")

  "reads" when {
    "passed valid JSON" should {
      "return a valid model" in {
        retrieveForeignReliefsBody shouldBe json.as[Def1_RetrieveForeignReliefsResponse]
      }
    }
  }

  "writes" when {
    "passed valid model" should {
      "return valid JSON" in {
        Json.toJson(retrieveForeignReliefsBody) shouldBe json
      }
    }
  }

}
