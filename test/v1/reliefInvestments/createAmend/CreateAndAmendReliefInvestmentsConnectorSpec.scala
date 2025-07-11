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

package v1.reliefInvestments.createAmend

import play.api.Configuration
import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v1.fixtures.CreateAndAmendReliefInvestmentsFixtures._
import v1.reliefInvestments.createAmend.def1.model.request.Def1_CreateAndAmendReliefInvestmentsRequestData
import v1.reliefInvestments.createAmend.model.request.CreateAndAmendReliefInvestmentsRequestData
import uk.gov.hmrc.http.StringContextOps

import scala.concurrent.Future

class CreateAndAmendReliefInvestmentsConnectorSpec extends ConnectorSpec {

  val nino = "ZG903729C"

  trait Test { _: ConnectorTest =>

    val taxYear: String

    val connector: CreateAndAmendReliefInvestmentsConnector = new CreateAndAmendReliefInvestmentsConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

    lazy val request: CreateAndAmendReliefInvestmentsRequestData =
      Def1_CreateAndAmendReliefInvestmentsRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBodyModel)

  }

  "doConnector" must {

    "put a body and return 204 no body - IFS enabled" in new IfsTest with Test {
      val taxYear: String                                = "2019-20"
      val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

      willPut(
        url = url"$baseUrl/income-tax/reliefs/investment/$nino/2019-20",
        body = requestBodyModel
      )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }

    "put a body and return 204 no body - HIP enabled for TYS" in new HipTest with Test {
      val taxYear: String = "2023-24"
      MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1924.enabled" -> true)

      val outcome: Right[Nothing, ResponseWrapper[Unit]] = Right(ResponseWrapper(correlationId, ()))

      willPut(
        url = url"$baseUrl/itsa/income-tax/v1/23-24/reliefs/investment/$nino",
        body = requestBodyModel
      ).returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }

    "put a body and return 204 no body for a Tax Year Specific (TYS) tax year - IFS enabled" in new IfsTest with Test {
      MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1924.enabled" -> false)

      val taxYear: String = "2023-24"
      val outcome         = Right(ResponseWrapper(correlationId, ()))

      willPut(
        url = url"$baseUrl/income-tax/reliefs/investment/23-24/$nino",
        body = requestBodyModel
      )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }

    "put a body and return 204 no body for a Tax Year Specific (TYS) tax year" in new HipTest with Test {
      MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1924.enabled" -> true)

      val taxYear: String = "2023-24"
      val outcome         = Right(ResponseWrapper(correlationId, ()))

      willPut(
        url = url"$baseUrl/itsa/income-tax/v1/23-24/reliefs/investment/$nino",
        body = requestBodyModel
      )
        .returns(Future.successful(outcome))

      await(connector.amend(request)) shouldBe outcome
    }
  }

}
