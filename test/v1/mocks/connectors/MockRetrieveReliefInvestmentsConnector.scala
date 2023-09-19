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

package v1.mocks.connectors

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.RetrieveReliefInvestmentsConnector
import v1.models.request.retrieveReliefInvestments.RetrieveReliefInvestmentsRequestData
import v1.models.response.retrieveReliefInvestments._

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveReliefInvestmentsConnector extends MockFactory {

  val mockConnector: RetrieveReliefInvestmentsConnector = mock[RetrieveReliefInvestmentsConnector]

  object MockRetrieveReliefInvestmentsConnector {

    def retrieve(requestData: RetrieveReliefInvestmentsRequestData): CallHandler4[
      RetrieveReliefInvestmentsRequestData,
      HeaderCarrier,
      ExecutionContext,
      String,
      Future[DownstreamOutcome[RetrieveReliefInvestmentsResponse]]] = {
      (mockConnector
        .retrieve(_: RetrieveReliefInvestmentsRequestData)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
