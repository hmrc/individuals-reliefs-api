package api.models.outcomes

case class ResponseWrapper[+A](correlationId: String, responseData: A) {
  def map[B](f: A => B): ResponseWrapper[B] = ResponseWrapper(correlationId, f(responseData))
}
