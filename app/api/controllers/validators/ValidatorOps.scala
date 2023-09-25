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

  /** Validates fields with their associated paths.
    *
    * @param fieldsWithPaths
    *   A sequence of fields with their associated paths.
    * @param validate
    *   A function to validate each field with its path.
    * @tparam A
    *   The type of the field.
    * @tparam B
    *   The type of the validation result.
    * @return
    *   A validation result.
    */
  protected def validateWithPaths[A, B](fieldsWithPaths: (Option[A], String)*)(
      validate: (A, Option[String]) => Validated[Seq[MtdError], B]): Validated[Seq[MtdError], Unit] =
    fieldsWithPaths
      .map {
        case (None, _)           => Valid(())
        case (Some(field), path) => validate(field, Some(path))
      }
      .sequence
      .andThen(_ => Valid(()))

  /** Provides utility operations for validating optional fields.
    *
    * @param maybeFields
    *   An optional field.
    * @tparam A
    *   The type of the field.
    */
  implicit class ValidatorOptionOps[A](maybeFields: Option[A]) {

    /** Maps the field to a validation result or returns a default valid value.
      *
      * @param mapping
      *   A function to map the field to a validation result.
      * @param defaultValid
      *   A default validation result.
      * @tparam B
      *   The type of the validation result.
      * @return
      *   A validation result.
      */
    def mapOrElse[B](mapping: A => Validated[Seq[MtdError], B], defaultValid: Valid[B]): Validated[Seq[MtdError], B] =
      maybeFields.map(mapping).getOrElse(defaultValid)

    /** Maps the field to a validation result or returns a default valid value of type Valid[Unit].
      *
      * @param mapping
      *   A function to map the field to a validation result.
      * @return
      *   A validation result.
      */
    def mapOrElse(mapping: A => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
      mapOrElse(mapping, Valid(()))

  }

  /** Provides utility operations for validating sequences of fields.
    *
    * @param fields
    *   A sequence of fields.
    * @tparam A
    *   The type of the field.
    */
  implicit class ValidatorSeqOps[A](fields: Seq[A]) {

    /** Validates each field in the sequence with its index and returns a validation result.
      *
      * @param validate
      *   A function to validate each field with its index.
      * @return
      *   A validation result.
      */
    def zipAndValidate(validate: (A, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
      fields.zipWithIndex.traverse(validate.tupled).map(_ => ())

  }

  /** Provides utility operations for validating optional sequences of fields.
    *
    * @param maybeFields
    *   An optional sequence of fields.
    * @tparam A
    *   The type of the field.
    */
  implicit class ValidatorOptionSeqOps[A](maybeFields: Option[Seq[A]]) {

    /** Validates each field in the optional sequence with its index or returns a default value.
      *
      * @param validate
      *   A function to validate each field with its index.
      * @param defaultValid
      *   A default validation result.
      * @tparam B
      *   The type of the validation result.
      * @return
      *   A validation result.
      */
    def zipAndValidate[B](validate: (A, Int) => Validated[Seq[MtdError], B], defaultValid: Valid[Seq[B]]): Validated[Seq[MtdError], Seq[B]] =
      maybeFields.mapOrElse((fields: Seq[A]) => fields.zipWithIndex.traverse(validate.tupled), defaultValid)

    /** Validates each field in the optional sequence with its index or returns a default value of Valid[Unit].
      *
      * @param validate
      *   A function to validate each field with its index.
      * @return
      *   A validation result.
      */
    def zipAndValidate(validate: (A, Int) => Validated[Seq[MtdError], Unit]): Validated[Seq[MtdError], Unit] =
      maybeFields.mapOrElse((fields: Seq[A]) => fields.zipWithIndex.traverse(validate.tupled).map(_ => ()))

  }

}
