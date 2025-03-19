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

package v2.charitableGiving.delete

import play.api.Configuration
import play.api.libs.json.JsObject
import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v2.charitableGiving.delete.model.request.Def1_DeleteCharitableGivingTaxReliefsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class DeleteCharitableGivingReliefConnectorSpec extends ConnectorSpec {

  private val nino = "ZG903729C"

  "delete()" should {
    "return a success response" when {
      "given a non-TYS request" when {
        "isPassDeleteIntentEnabled feature switch is on" in new IfsTest with Test {
          override val intent: Option[String] = Some("DELETE")

          MockedSharedAppConfig.featureSwitchConfig
            .returns(
              Configuration(
                "passDeleteIntentHeader.enabled" -> true
              ))
            .anyNumberOfTimes()

          willPost(
            url = url"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/2020",
            body = JsObject.empty
          )
            .returns(Future.successful(expectedOutcome))

          val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2019-20"))
          val result  = await(connector.delete(request))

          result shouldBe expectedOutcome
        }

        "isPassDeleteIntentEnabled feature switch is off" in new IfsTest with Test {
          MockedSharedAppConfig.featureSwitchConfig
            .returns(
              Configuration(
                "passDeleteIntentHeader.enabled" -> false
              ))
            .anyNumberOfTimes()

          willPost(
            url = url"$baseUrl/income-tax/nino/$nino/income-source/charity/annual/2020",
            body = JsObject.empty
          )
            .returns(Future.successful(expectedOutcome))

          val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2019-20"))
          val result  = await(connector.delete(request))

          result shouldBe expectedOutcome
        }
      }

      "given a TYS request" when {
        "isPassDeleteIntentEnabled feature switch is on" in new IfsTest with Test {
          override val intent: Option[String] = Some("DELETE")

          MockedSharedAppConfig.featureSwitchConfig returns Configuration(
            "passDeleteIntentHeader.enabled" -> true
          )

          willDelete(
            url = url"$baseUrl/income-tax/23-24/$nino/income-source/charity/annual"
          )
            .returns(Future.successful(expectedOutcome))

          val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2023-24"))
          val result  = await(connector.delete(request))

          result shouldBe expectedOutcome
        }

        "isPassDeleteIntentEnabled feature switch is off" in new IfsTest with Test {

          MockedSharedAppConfig.featureSwitchConfig returns Configuration(
            "passDeleteIntentHeader.enabled" -> false
          )

          willDelete(url = url"$baseUrl/income-tax/23-24/$nino/income-source/charity/annual")
            .returns(Future.successful(expectedOutcome))

          val request = Def1_DeleteCharitableGivingTaxReliefsRequestData(Nino(nino), TaxYear.fromMtd("2023-24"))
          val result  = await(connector.delete(request))

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
      appConfig = mockSharedAppConfig
    )

  }
}
