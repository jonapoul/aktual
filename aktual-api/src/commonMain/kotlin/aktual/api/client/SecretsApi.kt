package aktual.api.client

import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.SecretResponse

/**
 * Sets the credentials the server uses to reach each bank sync provider. They apply to every budget
 * on the server, so only admins can set them.
 *
 * See packages/sync-server/src/app-secrets.js
 */
interface SecretsApi {
  suspend fun set(secret: BankSyncSecret, value: String?): SecretResponse
}
