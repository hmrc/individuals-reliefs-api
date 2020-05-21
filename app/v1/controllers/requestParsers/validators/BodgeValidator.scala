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

    def toFieldNameMap[T <: Product](r: T): Map[String, Any] = {
      r.getClass.getDeclaredFields.map(_.getName).zip(r.productIterator.to).toMap
    }

    def generateFailedPaths[T <: Product](r: T): Seq[String] = {
      val map: Map[String, Any] = toFieldNameMap(r)

      map.collect {
        case (objectName, list: List[T]) =>
          list.zipWithIndex.flatMap {
            case (innerObject, i) =>
              toFieldNameMap(innerObject).collect {
                case (fieldName, Some(value: BigDecimal)) => SomeValidation.validate(value, s"$objectName/[$i]/$fieldName")
              }
          }
      }
    }.flatten.flatten.toSeq.sorted

    val formatValueErrors = {
      generateFailedPaths(body) match {
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
