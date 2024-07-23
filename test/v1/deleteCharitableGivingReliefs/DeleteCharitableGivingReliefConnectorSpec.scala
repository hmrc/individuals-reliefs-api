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

package v1.deleteCharitableGivingReliefs

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.libs.json.JsObject
import v1.deleteCharitableGivingReliefs.model.request.Def1_DeleteCharitableGivingTaxReliefsRequestData

import scala.concurrent.Future

class DeleteCharitableGivingReliefConnectorSpec extends ConnectorSpec {

  private val nino = "AA123456A"

  "delete()" should {
    "return a success response" when {
      "given a non-TYS request" when {
        "isPassDeleteIntentEnabled feature switch is on" in new DesTest with Test {
          override lazy val requiredHeaders: Seq[(String, String)] = requiredDesHeaders :+ ("intent" -> "DELETE")

          MockedAppConfig.featureSwitches returns Configuration(
            "passDeleteIntentHeader.enabled" -> true,
            "desIf_Migration.enabled"        -> false
          )

          willPost(
            url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/2020",
            body = JsObject.empty
          )
            .returns(Future.successful(expectedOutcome))

          private val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2019-20"))
          private val result  = await(connector.delete(request))

          result shouldBe expectedOutcome
        }

        "isPassDeleteIntentEnabled feature switch is off" in new DesTest with Test {
          override lazy val excludedHeaders: Seq[(String, String)] = super.excludedHeaders :+ ("intent" -> "DELETE")

          MockedAppConfig.featureSwitches returns Configuration(
            "passDeleteIntentHeader.enabled" -> false,
            "desIf_Migration.enabled"        -> false
          )

          willPost(
            url = s"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/2020",
            body = JsObject.empty
          )
            .returns(Future.successful(expectedOutcome))

          private val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2019-20"))
          private val result  = await(connector.delete(request))

          result shouldBe expectedOutcome
        }
      }

      "given a TYS request" when {
        "isPassDeleteIntentEnabled feature switch is on" in new TysIfsTest with Test {
          override lazy val requiredHeaders: Seq[(String, String)] = requiredTysIfsHeaders :+ ("intent" -> "DELETE")

          MockedAppConfig.featureSwitches returns Configuration(
            "passDeleteIntentHeader.enabled" -> true
          )

          willDelete(
            url = s"$baseUrl/income-tax/23-24/$nino/income-source/charity/annual"
          )
            .returns(Future.successful(expectedOutcome))

          private val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2023-24"))
          private val result  = await(connector.delete(request))

          result shouldBe expectedOutcome
        }

        "isPassDeleteIntentEnabled feature switch is off" in new TysIfsTest with Test {
          override lazy val excludedHeaders: Seq[(String, String)] = super.excludedHeaders :+ ("intent" -> "DELETE")

          MockedAppConfig.featureSwitches returns Configuration(
            "passDeleteIntentHeader.enabled" -> false
          )

          willDelete(url = s"$baseUrl/income-tax/23-24/$nino/income-source/charity/annual")
            .returns(Future.successful(expectedOutcome))

          private val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2023-24"))
          private val result  = await(connector.delete(request))

          result shouldBe expectedOutcome
        }
      }

    }
  }

  trait Test {
    _: ConnectorTest =>

    protected val expectedOutcome = Right(ResponseWrapper(correlationId, ()))

    protected val connector: DeleteCharitableGivingReliefConnector = new DeleteCharitableGivingReliefConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

}
