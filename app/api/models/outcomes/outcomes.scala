package api.models.outcomes

import api.models.auth.UserDetails
import api.models.errors.MtdError

package object outcomes {

  type AuthOutcome = Either[MtdError, UserDetails]

}
