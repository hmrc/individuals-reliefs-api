package api.models.hateoas

object RelType {
  val SELF                                      = "self"
  val CREATE_AMEND_RELIEF_INVESTMENTS           = "create-and-amend-reliefs-investments"
  val DELETE_RELIEF_INVESTMENTS                 = "delete-reliefs-investments"
  val CREATE_AMEND_RELIEFS_FOREIGN              = "create-and-amend-reliefs-foreign"
  val DELETE_RELIEFS_FOREIGN                    = "delete-reliefs-foreign"
  val AMEND_RELIEFS_OTHER                       = "create-and-amend-reliefs-other"
  val DELETE_RELIEFS_OTHER                      = "delete-reliefs-other"
  val AMEND_RELIEFS_PENSIONS                    = "create-and-amend-reliefs-pensions"
  val DELETE_RELIEFS_PENSIONS                   = "delete-reliefs-pensions"
  val CREATE_AMEND_CHARITABLE_GIVING_TAX_RELIEF = "create-and-amend-charitable-giving-tax-relief"
  val DELETE_CHARITABLE_GIVING_TAX_RELIEF       = "delete-charitable-giving-tax-relief"
}
