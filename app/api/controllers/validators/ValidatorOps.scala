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

package api.controllers.validators

import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.toTraverseOps

trait ValidatorOps {

  protected def validateWithPaths[A, B](fieldsWithPaths: (Option[A], String)*)(
      validate: (A, Option[String]) => Validated[Seq[MtdError], B]): Validated[Seq[MtdError], Unit] =
    fieldsWithPaths
      .map {
        case (None, _)           => Valid(())
        case (Some(field), path) => validate(field, Some(path))
      }
      .sequence
      .andThen(_ => Valid(()))

  implicit class ValidatorOptionOps[A](maybeFields: Option[A]) {

    def mapOrElse[B](mapping: A => Validated[Seq[MtdError], B], defaultValid: Valid[B]): Validated[Seq[MtdError], B] =
      maybeFields.map(mapping).getOrElse(defaultValid)

    def mapOrElse(mapping: A => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
      mapOrElse(mapping, Valid(()))

  }

  implicit class ValidatorSeqOps[A](fields: Seq[A]) {

    def zipAndValidate(validate: (A, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
      fields.zipWithIndex.traverse(validate.tupled).map(_ => ())

  }

  implicit class ValidatorOptionSeqOps[A](maybeFields: Option[Seq[A]]) {

    def zipAndValidate[B](validate: (A, Int) => Validated[Seq[MtdError], B], defaultValid: Valid[Seq[B]]): Validated[Seq[MtdError], Seq[B]] =
      maybeFields.mapOrElse((fields: Seq[A]) => fields.zipWithIndex.traverse(validate.tupled), defaultValid)

    def zipAndValidate(validate: (A, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
      maybeFields.mapOrElse((fields: Seq[A]) => fields.zipWithIndex.traverse(validate.tupled).map(_ => ()))

  }

}
