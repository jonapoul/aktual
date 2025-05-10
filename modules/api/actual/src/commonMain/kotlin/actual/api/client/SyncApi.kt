package actual.api.client

import actual.account.model.LoginToken
import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.GetUserKeyRequest
import actual.api.model.sync.GetUserKeyResponse
import actual.api.model.sync.ListUserFilesResponse
import actual.budget.model.BudgetId
import actual.url.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse

interface SyncApi {
  suspend fun fetchUserFiles(token: LoginToken): ListUserFilesResponse.Success
  suspend fun downloadUserFile(token: LoginToken, budgetId: BudgetId): HttpResponse
  suspend fun fetchUserFileInfo(token: LoginToken, budgetId: BudgetId): HttpResponse // TODO
  suspend fun fetchUserKey(token: LoginToken, body: GetUserKeyRequest): GetUserKeyResponse.Success
}

fun SyncApi(url: ServerUrl, client: HttpClient): SyncApi = SyncApiClient(url, client)

private class SyncApiClient(private val serverUrl: ServerUrl, private val client: HttpClient) : SyncApi {
  override suspend fun fetchUserFiles(token: LoginToken) = client
    .get(serverUrl, path = "/sync/list-user-files", headers = mapOf(ActualHeaders.TOKEN to token.value))
    .body<ListUserFilesResponse.Success>()

  override suspend fun downloadUserFile(token: LoginToken, budgetId: BudgetId) = client.get(
    serverUrl = serverUrl,
    path = "/sync/download-user-file",
    headers = mapOf(ActualHeaders.TOKEN to token.value, ActualHeaders.FILE_ID to budgetId.value),
  )

  // TODO: Implement properly?
  override suspend fun fetchUserFileInfo(token: LoginToken, budgetId: BudgetId) = client.get(
    serverUrl = serverUrl,
    path = "/sync/get-user-file-info",
    headers = mapOf(
      ActualHeaders.TOKEN to token.value,
      ActualHeaders.FILE_ID to budgetId.value,
    ),
  )

  override suspend fun fetchUserKey(token: LoginToken, body: GetUserKeyRequest) = client
    .post(serverUrl, body, "/sync/user-get-key", headers = mapOf(ActualHeaders.TOKEN to token.value))
    .body<GetUserKeyResponse.Success>()
}
