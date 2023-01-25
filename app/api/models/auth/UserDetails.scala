package api.models.auth

case class UserDetails(mtdId: String, userType: String, agentReferenceNumber: Option[String])
