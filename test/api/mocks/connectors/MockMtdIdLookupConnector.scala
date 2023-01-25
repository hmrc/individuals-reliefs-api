package api.mocks.connectors

import api.connectors.MtdIdLookupConnector
import api.connectors.connectors.MtdIdLookupOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockMtdIdLookupConnector extends MockFactory {

  val mockMtdIdLookupConnector: MtdIdLookupConnector = mock[MtdIdLookupConnector]

  object MockedMtdIdLookupConnector {

    def lookup(nino: String): CallHandler[Future[MtdIdLookupOutcome]] = {
      (mockMtdIdLookupConnector
        .getMtdId(_: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, *, *)
    }

  }

}
