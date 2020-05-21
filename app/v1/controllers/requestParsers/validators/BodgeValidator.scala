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

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError, ValueFormatErrorGenerator}
import v1.models.requestData.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRawData}

class BodgeValidator extends Validator[AmendReliefInvestmentsRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = (data: AmendReliefInvestmentsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      JsonFormatValidation.validate[AmendReliefInvestmentsBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  //noinspection ScalaStyle
  private def bodyFieldValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = { data =>
    val body = data.body.as[AmendReliefInvestmentsBody]

    // for every object in vctSubscription, add index and validate relevant fields
    // need index to show which object in the array contains the borked field(s)
    // SomeValidation.validate isn't accurate for the story, it just returns the path you put in if the input value <= 0
    val vctSubscriptionValueErrors: Seq[String] = body.vctSubscription.zipWithIndex.flatMap {
      case (item, i) =>
        item.amountInvested.map(SomeValidation.validate(_, s"vctSubscription[$i].amountInvested")).getOrElse(Nil) ++
        item.reliefClaimed.map(SomeValidation.validate(_, s"vctSubscription[$i].reliefClaimed")).getOrElse(Nil)
    }

    // same with eisSubscription
    val eisSubscriptionValueErrors: Seq[String] = body.eisSubscription.zipWithIndex.flatMap {
      case (item, i) =>
        item.amountInvested.map(SomeValidation.validate(_, s"eisSubscription[$i].amountInvested")).getOrElse(Nil) ++
        item.reliefClaimed.map(SomeValidation.validate(_, s"eisSubscription[$i].reliefClaimed")).getOrElse(Nil)
    }

    // same with communityInvestment
    val communityInvestmentValueErrors: Seq[String] = body.communityInvestment.zipWithIndex.flatMap {
      case (item, i) =>
        item.amountInvested.map(SomeValidation.validate(_, s"communityInvestment[$i].amountInvested")).getOrElse(Nil) ++
        item.reliefClaimed.map(SomeValidation.validate(_, s"communityInvestment[$i].reliefClaimed")).getOrElse(Nil)
    }

    // same with seedEnterpriseInvestment
    val seedEnterpriseInvestmentValueErrors: Seq[String] = body.seedEnterpriseInvestment.zipWithIndex.flatMap {
      case (item, i) =>
        item.amountInvested.map(SomeValidation.validate(_, s"seedEnterpriseInvestment[$i].amountInvested")).getOrElse(Nil) ++
        item.reliefClaimed.map(SomeValidation.validate(_, s"seedEnterpriseInvestment[$i].reliefClaimed")).getOrElse(Nil)
    }

    // same with socialEnterpriseInvestment
    val socialEnterpriseInvestmentValueErrors: Seq[String] = body.socialEnterpriseInvestment.zipWithIndex.flatMap {
      case (item, i) =>
        item.amountInvested.map(SomeValidation.validate(_, s"socialEnterpriseInvestment[$i].amountInvested")).getOrElse(Nil) ++
        item.reliefClaimed.map(SomeValidation.validate(_, s"socialEnterpriseInvestment[$i].reliefClaimed")).getOrElse(Nil)
    }

    val formatValueErrors = {
      // combine all the validations above together
      (vctSubscriptionValueErrors
        ++ eisSubscriptionValueErrors
        ++ communityInvestmentValueErrors
        ++ seedEnterpriseInvestmentValueErrors
        ++ socialEnterpriseInvestmentValueErrors
        ) match {
        case Nil =>
          // if the combined list is empty, return an empty list
          Nil
        case paths =>
          // if the combined list is not empty, return it in the paths of the FORMAT_VALUE error
          List(ValueFormatErrorGenerator.generate(paths))
      }
    }

    List(
      // all other errors you'd do on these things I guess
      // I support you'd do this kind of validation on all fields (FORMAT_NAME, etc), not just the VALUE ones...
      // check with BA, I don't remember off the top of my head
      formatValueErrors
    )
  }

  override def validate(data: AmendReliefInvestmentsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
