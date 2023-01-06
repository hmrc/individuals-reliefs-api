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

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.{DeleteForeignReliefsConnector, DownstreamOutcome}
import v1.models.request.deleteForeignReliefs.DeleteForeignReliefsRequest

import scala.concurrent.{ExecutionContext, Future}

trait MockDeleteForeignReliefsConnector extends MockFactory {

  val mockConnector: DeleteForeignReliefsConnector = mock[DeleteForeignReliefsConnector]

  object MockDeleteForeignReliefsConnector {

    def delete(requestData: DeleteForeignReliefsRequest): CallHandler[Future[DownstreamOutcome[Unit]]] = {
      (mockConnector
        .delete(_: DeleteForeignReliefsRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
