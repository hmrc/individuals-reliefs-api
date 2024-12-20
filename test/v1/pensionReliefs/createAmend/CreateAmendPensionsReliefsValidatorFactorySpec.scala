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

package v1.pensionReliefs.createAmend

import play.api.libs.json.{JsValue, Json}
import shared.config.MockSharedAppConfig
import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v1.pensionReliefs.createAmend.def1.Def1_CreateAmendPensionsReliefsValidator

class CreateAmendPensionsReliefsValidatorFactorySpec extends UnitSpec with JsonErrorValidators with MockSharedAppConfig {

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"

  def requestBodyJson(): JsValue = Json.parse(
    s"""
       |{
       |
       |}
     """.stripMargin
  )

  private val validRequestBody = requestBodyJson()

  private val validatorFactory = new CreateAmendPensionsReliefsValidatorFactory

  "running a validation" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        val result = validatorFactory.validator(validNino, validTaxYear, validRequestBody)
        result shouldBe a[Def1_CreateAmendPensionsReliefsValidator]

      }
    }

  }

}
