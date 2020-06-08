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

package v1.controllers.requestParsers.validators.validations

import java.time.LocalDate

import v1.models.errors.{DateOfInvestmentFormatError, MtdError}

import scala.util.{Failure, Success, Try}

object DateValidation {

  def validateOptional(date: Option[String], path: String): List[MtdError] = {
    date match {
      case None => NoValidationErrors
      case Some(value) => validate(value, path)
    }
  }

  private def validate(date: String, path: String): List[MtdError] = Try {
    if(date.nonEmpty) LocalDate.parse(date, dateFormat)
  } match {
    case Success(_) => Nil
    case Failure(_) => List(
      DateOfInvestmentFormatError.copy(paths = Some(Seq(path)))
    )
  }


}