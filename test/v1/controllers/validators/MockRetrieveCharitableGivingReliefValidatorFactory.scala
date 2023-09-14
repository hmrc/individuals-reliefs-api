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

package v1.controllers.validators

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.models.request.retrieveCharitableGivingTaxRelief.RetrieveCharitableGivingReliefRequestData

trait MockRetrieveCharitableGivingReliefValidatorFactory extends MockFactory {

  val mockValidatorFactory: RetrieveCharitableGivingReliefValidatorFactory = mock[RetrieveCharitableGivingReliefValidatorFactory]

  object MockRetrieveCharitableGivingReliefValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveCharitableGivingReliefRequestData]] =
      (mockValidatorFactory.validator(_: String, _: String)).expects(*, *)
  }

  def willUseValidator(use: Validator[RetrieveCharitableGivingReliefRequestData]): CallHandler[Validator[RetrieveCharitableGivingReliefRequestData]] = {
    MockRetrieveCharitableGivingReliefValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveCharitableGivingReliefRequestData): Validator[RetrieveCharitableGivingReliefRequestData] =
    new Validator[RetrieveCharitableGivingReliefRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveCharitableGivingReliefRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveCharitableGivingReliefRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveCharitableGivingReliefRequestData] =
    new Validator[RetrieveCharitableGivingReliefRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveCharitableGivingReliefRequestData] = Invalid(result)
    }

}