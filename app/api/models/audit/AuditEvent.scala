package api.models.audit

case class AuditEvent[T](
    auditType: String,
    transactionName: String,
    detail: T
)
