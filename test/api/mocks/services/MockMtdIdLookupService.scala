package api.mocks.services

import api.connectors.connectors.MtdIdLookupOutcome
import api.services.MtdIdLookupService
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockMtdIdLookupService extends MockFactory {

  val mockMtdIdLookupService: MtdIdLookupService = mock[MtdIdLookupService]

  object MockedMtdIdLookupService {

    def lookup(nino: String): CallHandler[Future[MtdIdLookupOutcome]] = {
      (mockMtdIdLookupService
        .lookup(_: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, *, *)
    }

  }

}
