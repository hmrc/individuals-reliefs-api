package api.mocks

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import utils.IdGenerator

trait MockIdGenerator extends MockFactory {

  val mockIdGenerator: IdGenerator = mock[IdGenerator]

  object MockIdGenerator {
    def getCorrelationId: CallHandler[String] = (mockIdGenerator.getCorrelationId _).expects()
  }

}
