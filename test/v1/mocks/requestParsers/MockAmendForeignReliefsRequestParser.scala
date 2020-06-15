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

package v1.mocks.requestParsers

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v1.controllers.requestParsers.AmendForeignReliefsRequestParser
import v1.models.errors.ErrorWrapper
import v1.models.request.amendForeignReliefs.{AmendForeignReliefsRawData, AmendForeignReliefsRequest}

trait MockAmendForeignReliefsRequestParser extends MockFactory {

  val mockAmendForeignReliefsRequestParser: AmendForeignReliefsRequestParser = mock[AmendForeignReliefsRequestParser]

  object MockAmendForeignReliefsRequestParser {
    def parseRequest(data: AmendForeignReliefsRawData): CallHandler[Either[ErrorWrapper, AmendForeignReliefsRequest]] = {
      (mockAmendForeignReliefsRequestParser.parseRequest(_: AmendForeignReliefsRawData)).expects(data)
    }
  }

}