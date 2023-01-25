package api.mocks.services

import api.models.audit.AuditEvent
import api.services.AuditService
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.Writes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult

import scala.concurrent.{ExecutionContext, Future}

trait MockAuditService extends MockFactory {

  val mockAuditService: AuditService = stub[AuditService]

  object MockedAuditService {

    def verifyAuditEvent[T](event: AuditEvent[T]): CallHandler[Future[AuditResult]] = {
      (mockAuditService
        .auditEvent(_: AuditEvent[T])(_: HeaderCarrier, _: ExecutionContext, _: Writes[T]))
        .verify(event, *, *, *)
        .returning(Future.successful(AuditResult.Success))
    }

  }

}
