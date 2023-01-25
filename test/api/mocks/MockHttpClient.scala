package api.mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.{ExecutionContext, Future}

trait MockHttpClient extends MockFactory {

  val mockHttpClient: HttpClient = mock[HttpClient]

  object MockedHttpClient extends Matchers {

    def get[T](url: String,
               config: HeaderCarrier.Config,
               parameters: Seq[(String, String)] = Seq.empty,
               requiredHeaders: Seq[(String, String)] = Seq.empty,
               excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .GET(_: String, _: Seq[(String, String)], _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs {
          (actualUrl: String,
           actualParams: Seq[(String, String)],
           _: Seq[(String, String)],
           _: HttpReads[T],
           hc: HeaderCarrier,
           _: ExecutionContext) =>
            {
              actualUrl shouldBe url
              actualParams shouldBe parameters

              val headersForUrl = hc.headersForUrl(config)(actualUrl)
              assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
            }
        })
    }

    def post[I, T](url: String,
                   config: HeaderCarrier.Config,
                   body: I,
                   requiredHeaders: Seq[(String, String)] = Seq.empty,
                   excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .POST[I, T](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs { (actualUrl: String, actualBody: I, _, _, _, hc: HeaderCarrier, _) =>
          {
            actualUrl shouldBe url
            actualBody shouldBe body

            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
    }

    def put[I, T](url: String,
                  config: HeaderCarrier.Config,
                  body: I,
                  requiredHeaders: Seq[(String, String)] = Seq.empty,
                  excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .PUT[I, T](_: String, _: I, _: Seq[(String, String)])(_: Writes[I], _: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs { (actualUrl: String, actualBody: I, _, _, _, hc: HeaderCarrier, _) =>
          {
            actualUrl shouldBe url
            actualBody shouldBe body

            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
    }

    def delete[T](url: String,
                  config: HeaderCarrier.Config,
                  requiredHeaders: Seq[(String, String)] = Seq.empty,
                  excludedHeaders: Seq[(String, String)] = Seq.empty): CallHandler[Future[T]] = {
      (mockHttpClient
        .DELETE(_: String, _: Seq[(String, String)])(_: HttpReads[T], _: HeaderCarrier, _: ExecutionContext))
        .expects(assertArgs { (actualUrl: String, _, _, hc: HeaderCarrier, _) =>
          {
            actualUrl shouldBe url

            val headersForUrl = hc.headersForUrl(config)(actualUrl)
            assertHeaders(headersForUrl, requiredHeaders, excludedHeaders)
          }
        })
    }

    private def assertHeaders[T, I](actualHeaders: Seq[(String, String)],
                                    requiredHeaders: Seq[(String, String)],
                                    excludedHeaders: Seq[(String, String)]) = {

      actualHeaders should contain allElementsOf requiredHeaders
      actualHeaders should contain noElementsOf excludedHeaders
    }

  }

}
