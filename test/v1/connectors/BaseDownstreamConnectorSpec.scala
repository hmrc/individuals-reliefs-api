/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.connectors

import config.AppConfig
import mocks.MockAppConfig
import uk.gov.hmrc.http.{HttpReads, HttpClient}
import v1.connectors.DownstreamUri.{DesUri, IfsUri}
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class BaseDownstreamConnectorSpec extends ConnectorSpec {

  // WLOG
  case class Result(value: Int)

  // WLOG
  val body = "body"

  val outcome = Right(ResponseWrapper(correlationId, Result(2)))

  val url = "some/url?param=value"
  val absoluteUrl = s"$baseUrl/$url"

  implicit val httpReads: HttpReads[DesOutcome[Result]] = mock[HttpReads[DesOutcome[Result]]]

  class DesTest extends MockHttpClient with MockAppConfig {
    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }
    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnv returns "des-environment"
  }

  class IfsTest extends MockHttpClient with MockAppConfig {
    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }
    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnv returns "ifs-environment"
  }

  "for DES" when {
    "post" must {
      "posts with the required des headers and returns the result" in new DesTest {
        MockedHttpClient
          .post(absoluteUrl, body, "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
          .returns(Future.successful(outcome))

        await(connector.post(body, DesUri[Result](url))) shouldBe outcome
      }
    }

    "get" must {
      "get with the required des headers and return the result" in new DesTest {
        MockedHttpClient
          .get(absoluteUrl, "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
          .returns(Future.successful(outcome))

        await(connector.get(DesUri[Result](url))) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required des headers and return the result" in new DesTest {
        MockedHttpClient
          .delete(absoluteUrl, requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
          .returns(Future.successful(outcome))

        await(connector.delete(DesUri[Result](url))) shouldBe outcome
      }
    }

    "put" must {
      "put with the required des headers and return result" in new DesTest {
        MockedHttpClient.put(absoluteUrl, body, requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")
          .returns(Future.successful(outcome))

        await(connector.put(body, DesUri[Result](url))) shouldBe outcome
      }
    }
  }

  "for Ifs" when {
    "post" must {
      "posts with the required des headers and returns the result" in new IfsTest {
        MockedHttpClient
          .post(absoluteUrl, body, requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token")
          .returns(Future.successful(outcome))

        await(connector.post(body, IfsUri[Result](url))) shouldBe outcome
      }
    }

    "get" must {
      "get with the required ifs headers and return the result" in new IfsTest {
        MockedHttpClient
          .get(absoluteUrl, requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token")
          .returns(Future.successful(outcome))

        await(connector.get(IfsUri[Result](url))) shouldBe outcome
      }
    }

    "delete" must {
      "delete with the required ifs headers and return the result" in new IfsTest {
        MockedHttpClient
          .delete(absoluteUrl, requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token")
          .returns(Future.successful(outcome))

        await(connector.delete(IfsUri[Result](url))) shouldBe outcome
      }
    }

    "put" must {
      "put with the required ifs headers and return result" in new IfsTest {
        MockedHttpClient.put(absoluteUrl, body, requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token")
          .returns(Future.successful(outcome))

        await(connector.put(body, IfsUri[Result](url))) shouldBe outcome
      }
    }
  }
}
