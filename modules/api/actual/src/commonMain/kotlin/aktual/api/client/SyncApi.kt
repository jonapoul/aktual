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
package aktual.api.client

import aktual.api.model.internal.AktualHeaders
import aktual.api.model.sync.DeleteUserFileRequest
import aktual.api.model.sync.DeleteUserFileResponse
import aktual.api.model.sync.GetUserFileInfoResponse
import aktual.api.model.sync.GetUserKeyRequest
import aktual.api.model.sync.GetUserKeyResponse
import aktual.api.model.sync.ListUserFilesResponse
import aktual.budget.model.BudgetId
import aktual.codegen.Body
import aktual.codegen.GET
import aktual.codegen.Header
import aktual.codegen.KtorApi
import aktual.codegen.POST
import aktual.core.model.LoginToken
import aktual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface SyncApi {
  @GET("/sync/list-user-files")
  suspend fun fetchUserFiles(
    @Header(AktualHeaders.TOKEN) token: LoginToken,
  ): ListUserFilesResponse.Success

  @GET("/sync/get-user-file-info")
  suspend fun fetchUserFileInfo(
    @Header(AktualHeaders.TOKEN) token: LoginToken,
    @Header(AktualHeaders.FILE_ID) budgetId: BudgetId,
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
