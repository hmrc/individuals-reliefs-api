/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.hateoas

object RelType {
  val SELF                      = "self"
  val AMEND_RELIEF_INVESTMENTS  = "create-and-amend-reliefs-investments"
  val DELETE_RELIEF_INVESTMENTS = "delete-reliefs-investments"
  val AMEND_RELIEFS_FOREIGN     = "create-and-amend-reliefs-foreign"
  val DELETE_RELIEFS_FOREIGN    = "delete-reliefs-foreign"
  val AMEND_RELIEFS_OTHER       = "create-and-amend-reliefs-other"
  val DELETE_RELIEFS_OTHER      = "delete-reliefs-other"
  val AMEND_RELIEFS_PENSIONS    = "create-and-amend-reliefs-pensions"
  val DELETE_RELIEFS_PENSIONS   = "delete-reliefs-pensions"
  val CREATE_AMEND_CHARITABLE_GIVING_TAX_RELIEF = "create-and-amend-charitable-giving-tax-relief"
  val DELETE_CHARITABLE_GIVING_TAX_RELIEF = "delete-charitable-giving-tax-relief"
}
