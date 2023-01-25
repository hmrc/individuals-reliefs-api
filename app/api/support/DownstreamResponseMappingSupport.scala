package api.support

import api.controllers.EndpointLogContext
import api.models
import api.models.errors
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import utils.Logging

trait DownstreamResponseMappingSupport {
  self: Logging =>

  final def mapDownstreamErrors[D](errorCodeMap: PartialFunction[String, MtdError])(responseWrapper: ResponseWrapper[DownstreamError])(implicit
      logContext: EndpointLogContext): ErrorWrapper = {

    lazy val defaultErrorCodeMapping: String => MtdError = { code =>
      logger.warn(s"[${logContext.controllerName}] [${logContext.endpointName}] - No mapping found for error code $code")
      errors.InternalError
    }

    responseWrapper match {
      case ResponseWrapper(correlationId, DownstreamErrors(error :: Nil)) =>
        errors.ErrorWrapper(correlationId, errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping), None)

      case ResponseWrapper(correlationId, DownstreamErrors(errorCodes)) =>
        val mtdErrors = errorCodes.map(error => errorCodeMap.applyOrElse(error.code, defaultErrorCodeMapping))

        if (mtdErrors.contains(errors.InternalError)) {
          logger.warn(
            s"[${logContext.controllerName}] [${logContext.endpointName}] [CorrelationId - $correlationId]" +
              s" - downstream returned ${errorCodes.map(_.code).mkString(",")}. Revert to ISE")
          ErrorWrapper(correlationId, errors.InternalError, None)
        } else {
          errors.ErrorWrapper(correlationId, BadRequestError, Some(mtdErrors))
        }

      case ResponseWrapper(correlationId, OutboundError(error, errors)) =>
        models.errors.ErrorWrapper(correlationId, error, errors)
    }
  }

}
