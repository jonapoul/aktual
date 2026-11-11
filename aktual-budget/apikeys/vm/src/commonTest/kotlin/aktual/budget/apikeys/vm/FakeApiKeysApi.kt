package aktual.budget.apikeys.vm

import aktual.api.client.ApiKeysApi
import aktual.api.model.apikeys.ApiKey
import aktual.api.model.apikeys.ApiKeyId
import aktual.api.model.apikeys.ApiKeysResponse
import aktual.api.model.apikeys.CreatedApiKey
import aktual.budget.model.BudgetId

internal class FakeApiKeysApi : ApiKeysApi {
  var listResponse: ApiKeysResponse<List<ApiKey>> = ApiKeysResponse.Success(emptyList())
  var createResponse: ApiKeysResponse<CreatedApiKey> = ApiKeysResponse.Failed(reason = null)
  var revokeResponse: ApiKeysResponse<Unit> = ApiKeysResponse.Success(Unit)

  val created = mutableListOf<Pair<String, List<BudgetId>>>()
  val revoked = mutableListOf<ApiKeyId>()

  override suspend fun list() = listResponse

  override suspend fun create(
    name: String,
    budgets: List<BudgetId>,
  ): ApiKeysResponse<CreatedApiKey> {
    created += name to budgets
    return createResponse
  }

  override suspend fun revoke(id: ApiKeyId): ApiKeysResponse<Unit> {
    revoked += id
    return revokeResponse
  }
}
