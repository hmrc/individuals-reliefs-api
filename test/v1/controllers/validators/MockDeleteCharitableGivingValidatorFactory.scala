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
import v1.models.request.deleteCharitableGivingTaxRelief.DeleteCharitableGivingTaxReliefRequestData

trait MockDeleteCharitableGivingValidatorFactory extends MockFactory {
  val mockDeleteCharitableGivingValidatorFactory: DeleteCharitableGivingValidatorFactory = mock[DeleteCharitableGivingValidatorFactory]

  object MockedDeleteCharitableGivingValidatorFactory {

    def validator(): CallHandler[Validator[DeleteCharitableGivingTaxReliefRequestData]] =
      (mockDeleteCharitableGivingValidatorFactory.validator(_: String, _: String)).expects(*, *)
  }

  def willUseValidator(use: Validator[DeleteCharitableGivingTaxReliefRequestData]): CallHandler[Validator[DeleteCharitableGivingTaxReliefRequestData]] = {
    MockedDeleteCharitableGivingValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: DeleteCharitableGivingTaxReliefRequestData): Validator[DeleteCharitableGivingTaxReliefRequestData] =
    new Validator[DeleteCharitableGivingTaxReliefRequestData] {
      def validate: Validated[Seq[MtdError], DeleteCharitableGivingTaxReliefRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[DeleteCharitableGivingTaxReliefRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[DeleteCharitableGivingTaxReliefRequestData] =
    new Validator[DeleteCharitableGivingTaxReliefRequestData] {
      def validate: Validated[Seq[MtdError], DeleteCharitableGivingTaxReliefRequestData] = Invalid(result)
    }
}
