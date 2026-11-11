package aktual.api.client

import aktual.api.model.apikeys.ApiKey
import aktual.api.model.apikeys.ApiKeyId
import aktual.api.model.apikeys.ApiKeysResponse
import aktual.api.model.apikeys.CreatedApiKey
import aktual.budget.model.BudgetId

/**
 * Creates, lists and revokes the signed-in user's budget API keys. A key can only download and sync
 * the budgets it was made for, and can't call these endpoints itself.
 *
 * See packages/sync-server/src/app-api-keys.ts
 */
interface ApiKeysApi {
  suspend fun list(): ApiKeysResponse<List<ApiKey>>

  suspend fun create(name: String, budgets: List<BudgetId>): ApiKeysResponse<CreatedApiKey>

  suspend fun revoke(id: ApiKeyId): ApiKeysResponse<Unit>
}
