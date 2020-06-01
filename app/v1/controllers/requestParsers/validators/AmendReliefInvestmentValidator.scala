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

import v1.controllers.requestParsers.validators.validations.{DateValidation, InvestmentRefValidation, JsonFormatValidation, NameValidation, NinoValidation, NumberValidation, TaxYearValidation}
import v1.models.errors.{FormatDateOfInvestmentErrorGenerator, FormatInvestmentRefErrorGenerator, FormatNameErrorGenerator, MtdError, RuleIncorrectOrEmptyBodyError, ValueFormatErrorGenerator}
import v1.models.requestData.amendReliefInvestments.{AmendReliefInvestmentsBody, AmendReliefInvestmentsRawData}


class AmendReliefInvestmentValidator extends Validator[AmendReliefInvestmentsRawData] {

  private val validationSet = List(parameterFormatValidation, bodyFieldValidation)

  private def parameterFormatValidation: AmendReliefInvestmentsRawData => List[List[MtdError]] = (data: AmendReliefInvestmentsRawData) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      JsonFormatValidation.validate[AmendReliefInvestmentsBody](data.body, RuleIncorrectOrEmptyBodyError),
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
                case (fieldName, Some(value: BigDecimal)) => NumberValidation.validate(value, s"$objectName/[$i]/$fieldName")
              }
          }
      }
    }.flatten.flatten.toSeq.sorted

    val formatValueErrors = {
      generateFailedPaths(body) match {
        case Nil =>
          Nil
        case paths =>
          List(ValueFormatErrorGenerator.generate(paths))
      }
    }

    def dateValidations[T <: Product](r: T): Seq[String] = {
      val map: Map[String, Any] = toFieldNameMap(r)

      map.collect {
        case (objectName, list: List[T]) =>
          list.zipWithIndex.flatMap {
            case (innerObject, i) =>
              toFieldNameMap(innerObject).collect {
                case (fieldName: String, Some(value: String)) if fieldName.take(4) == "date" => DateValidation.validate(value, s"$objectName/[$i]/$fieldName")
              }
          }
      }.flatten.flatten.toSeq.sorted

    }

    val dateFormatErrors = {
      dateValidations(body) match {
        case Nil =>
          Nil
        case paths =>
          List(FormatDateOfInvestmentErrorGenerator.generate(paths))
      }
    }

    def nameValidations[T <: Product](r: T): Seq[String] = {
      val map: Map[String, Any] = toFieldNameMap(r)

      map.collect {
        case (objectName, list: List[T]) =>
          list.zipWithIndex.flatMap {
            case (innerObject, i) =>
              toFieldNameMap(innerObject).collect {
                case (fieldName: String, Some(value: String)) if fieldName.take(4) == "name" || fieldName.takeRight(4) == "Name" => NameValidation.validate(value, s"$objectName/[$i]/$fieldName")
              }
          }
      }.flatten.flatten.toSeq.sorted
    }

    val nameFormatErrors = {
      nameValidations(body) match {
        case Nil =>
          Nil
        case paths =>
          List(FormatNameErrorGenerator.generate(paths))
      }

    }

    def investmentRefValidations[T <: Product](r: T): Seq[String] = {
      val map: Map[String, Any] = toFieldNameMap(r)

      map.collect {
        case (objectName, list: List[T]) =>
          list.zipWithIndex.flatMap {
            case (innerObject, i) =>
              toFieldNameMap(innerObject).collect {
                case (fieldName: String, Some(value: String)) if fieldName.takeRight(3) == "Ref" => InvestmentRefValidation.validate(value, s"$objectName/[$i]/$fieldName")
              }
          }
      }.flatten.flatten.toSeq.sorted
    }

    val investmentRefFormatErrors = {
      investmentRefValidations(body) match {
        case Nil =>
          Nil
        case paths =>
          List(FormatInvestmentRefErrorGenerator.generate(paths))
      }

    }

    List(
      formatValueErrors,
      dateFormatErrors,
      nameFormatErrors,
      investmentRefFormatErrors
    )
  }

  override def validate(data: AmendReliefInvestmentsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
