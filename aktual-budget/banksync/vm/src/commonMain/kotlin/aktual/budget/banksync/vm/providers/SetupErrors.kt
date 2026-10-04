package aktual.budget.banksync.vm.providers

import aktual.api.model.banksync.SecretResponse
import aktual.budget.banksync.vm.providers.SetupError.Other

// packages/desktop-client/src/util/secrets-errors.ts getSecretsError()
internal fun SecretResponse.Failed.toSetupError(): SetupError =
  when (reason) {
    SecretResponse.NOT_ADMIN -> NotAdmin
    SecretResponse.UNAUTHORIZED -> LoggedOut
    else -> Other(details ?: reason)
  }
