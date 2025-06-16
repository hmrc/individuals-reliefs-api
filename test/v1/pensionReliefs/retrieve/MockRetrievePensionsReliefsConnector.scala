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

package v1.pensionReliefs.retrieve

import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import shared.connectors.DownstreamOutcome
import uk.gov.hmrc.http.HeaderCarrier
import v1.pensionReliefs.retrieve.model.request.RetrievePensionsReliefsRequestData
import v1.pensionReliefs.retrieve.model.response.RetrievePensionsReliefsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrievePensionsReliefsConnector extends MockFactory { self: TestSuite =>

  val mockConnector: RetrievePensionsReliefsConnector = mock[RetrievePensionsReliefsConnector]

  object MockRetrievePensionsReliefsConnector {

    def retrieve(requestData: RetrievePensionsReliefsRequestData): CallHandler4[
      RetrievePensionsReliefsRequestData,
      HeaderCarrier,
      ExecutionContext,
      String,
      Future[DownstreamOutcome[RetrievePensionsReliefsResponse]]] = {
      (mockConnector
        .retrieve(_: RetrievePensionsReliefsRequestData)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
