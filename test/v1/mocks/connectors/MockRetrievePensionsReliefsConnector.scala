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

package v1.mocks.connectors

import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.{DownstreamOutcome, RetrievePensionsReliefsConnector}
import v1.models.request.retrievePensionsReliefs.RetrievePensionsReliefsRequest
import v1.models.response.retrievePensionsReliefs.RetrievePensionsReliefsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrievePensionsReliefsConnector extends MockFactory {

  val mockConnector: RetrievePensionsReliefsConnector = mock[RetrievePensionsReliefsConnector]

  object MockRetrievePensionsReliefsConnector {

    def retrieve(requestData: RetrievePensionsReliefsRequest): CallHandler4[
      RetrievePensionsReliefsRequest,
      HeaderCarrier,
      ExecutionContext,
      String,
      Future[DownstreamOutcome[RetrievePensionsReliefsResponse]]] = {
      (mockConnector
        .retrieve(_: RetrievePensionsReliefsRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
