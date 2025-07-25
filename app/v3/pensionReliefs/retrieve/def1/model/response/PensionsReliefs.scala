/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.pensionReliefs.retrieve.def1.model.response

import play.api.libs.json.{Json, OFormat}

case class PensionsReliefs(regularPensionContributions: Option[BigDecimal],
                           oneOffPensionContributionsPaid: Option[BigDecimal],
                           retirementAnnuityPayments: Option[BigDecimal],
                           paymentToEmployersSchemeNoTaxRelief: Option[BigDecimal],
                           overseasPensionSchemeContributions: Option[BigDecimal])

object PensionsReliefs {
  implicit val format: OFormat[PensionsReliefs] = Json.format[PensionsReliefs]
}
