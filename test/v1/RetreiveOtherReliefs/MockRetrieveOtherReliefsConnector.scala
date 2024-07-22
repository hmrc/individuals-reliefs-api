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

package v1.RetreiveOtherReliefs

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.RetreiveOtherReliefs.model.request.RetrieveOtherReliefsRequestData
import v1.RetreiveOtherReliefs.model.response.RetrieveOtherReliefsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveOtherReliefsConnector extends MockFactory {

  val mockConnector: RetrieveOtherReliefsConnector = mock[RetrieveOtherReliefsConnector]

  object MockRetrieveOtherReliefsConnector {

    def retrieve(requestData: RetrieveOtherReliefsRequestData): CallHandler4[
      RetrieveOtherReliefsRequestData,
      HeaderCarrier,
      ExecutionContext,
      String,
      Future[DownstreamOutcome[RetrieveOtherReliefsResponse]]] = {
      (mockConnector
        .retrieve(_: RetrieveOtherReliefsRequestData)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
