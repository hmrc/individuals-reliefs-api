/*
 * Copyright 2020 HM Revenue & Customs
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

package v1

import v1.models.errors.ErrorWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.response.retrieveForeignReliefs.RetrieveForeignReliefsBody
import v1.models.response.retrieveOtherReliefs.RetrieveOtherReliefsBody
import v1.models.response.retrieveReliefInvestments._

package object services {

  private type ServiceOutcome[A] = Either[ErrorWrapper, ResponseWrapper[A]]

  type AmendReliefInvestmentsServiceOutcome = ServiceOutcome[Unit]

  type AmendOtherReliefsServiceOutcome = ServiceOutcome[Unit]

  type DeleteReliefInvestmentsServiceOutcome = ServiceOutcome[Unit]

  type RetrieveReliefInvestmentsServiceOutcome = ServiceOutcome[RetrieveReliefInvestmentsBody]

  type DeleteOtherReliefsServiceOutcome = ServiceOutcome[Unit]

  type RetrieveOtherReliefsServiceOutcome = ServiceOutcome[RetrieveOtherReliefsBody]

  type AmendForeignReliefsServiceOutcome = ServiceOutcome[Unit]

  type DeleteForeignReliefsServiceOutcome = ServiceOutcome[Unit]

  type RetrieveForeignReliefsServiceOutcome = ServiceOutcome[RetrieveForeignReliefsBody]

  type AmendPensionsReliefsServiceOutcome = ServiceOutcome[Unit]

}
