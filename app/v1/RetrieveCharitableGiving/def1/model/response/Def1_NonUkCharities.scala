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

package v1.RetrieveCharitableGiving.def1.model.response

import play.api.libs.json.{Format, Json}

case class Def1_NonUkCharities(charityNames: Option[Seq[String]], totalAmount: BigDecimal)

object Def1_NonUkCharities {

  implicit val format: Format[Def1_NonUkCharities] = Json.format[Def1_NonUkCharities]

  /*private[response]*/
  def from(charityNames: Option[Seq[String]], totalAmount: Option[BigDecimal]): Option[Def1_NonUkCharities] =
    totalAmount.map(Def1_NonUkCharities(charityNames, _))

}
