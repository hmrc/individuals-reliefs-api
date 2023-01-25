package api.mocks.services

import api.models.auth.UserDetails
import api.models.outcomes.outcomes.AuthOutcome
import api.services.EnrolmentsAuthService
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockEnrolmentsAuthService extends MockFactory {

  val mockEnrolmentsAuthService: EnrolmentsAuthService = mock[EnrolmentsAuthService]

  object MockedEnrolmentsAuthService {

    def authoriseUser(): Unit = {
      (mockEnrolmentsAuthService
        .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returns(Future.successful(Right(UserDetails("mtd-id", "Individual", None))))
    }

    def authorised(predicate: Predicate): CallHandler[Future[AuthOutcome]] = {
      (mockEnrolmentsAuthService
        .authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *)
    }

  }

}
