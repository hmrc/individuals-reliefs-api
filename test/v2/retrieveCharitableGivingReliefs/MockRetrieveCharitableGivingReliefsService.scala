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

package v2.retrieveCharitableGivingReliefs

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import shared.controllers.RequestContext
import shared.models.errors.ErrorWrapper
import shared.models.outcomes.ResponseWrapper
import v2.retrieveCharitableGivingReliefs.model.request.Def1_RetrieveCharitableGivingReliefsRequestData
import v2.retrieveCharitableGivingReliefs.model.response.RetrieveCharitableGivingReliefsResponse

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveCharitableGivingReliefsService extends MockFactory {

  val mockRetrieveCharitableGivingReliefsService: RetrieveCharitableGivingReliefsService = mock[RetrieveCharitableGivingReliefsService]

  object MockRetrieveCharitableGivingReliefsService {

    def retrieve(request: Def1_RetrieveCharitableGivingReliefsRequestData)
        : CallHandler[Future[Either[ErrorWrapper, ResponseWrapper[RetrieveCharitableGivingReliefsResponse]]]] = {
      (
        mockRetrieveCharitableGivingReliefsService
          .retrieve(_: Def1_RetrieveCharitableGivingReliefsRequestData)(
            _: RequestContext,
            _: ExecutionContext
          )
        )
        .expects(request, *, *)
    }

  }

}
