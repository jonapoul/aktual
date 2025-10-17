/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.api.client

import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.DeleteUserFileRequest
import actual.api.model.sync.DeleteUserFileResponse
import actual.api.model.sync.GetUserFileInfoResponse
import actual.api.model.sync.GetUserKeyRequest
import actual.api.model.sync.GetUserKeyResponse
import actual.api.model.sync.ListUserFilesResponse
import actual.budget.model.BudgetId
import actual.codegen.Body
import actual.codegen.GET
import actual.codegen.Header
import actual.codegen.KtorApi
import actual.codegen.POST
import actual.core.model.LoginToken
import actual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface SyncApi {
  @GET("/sync/list-user-files")
  suspend fun fetchUserFiles(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): ListUserFilesResponse.Success

  @GET("/sync/get-user-file-info")
  suspend fun fetchUserFileInfo(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Header(ActualHeaders.FILE_ID) budgetId: BudgetId,
  ): GetUserFileInfoResponse.Success

  @POST("/sync/user-get-key")
  suspend fun fetchUserKey(
    @Body body: GetUserKeyRequest,
  ): GetUserKeyResponse.Success

  @POST("/sync/delete-user-file")
  suspend fun delete(
    @Body body: DeleteUserFileRequest,
  ): DeleteUserFileResponse.Success
}

expect fun SyncApi(serverUrl: ServerUrl, client: HttpClient): SyncApi
