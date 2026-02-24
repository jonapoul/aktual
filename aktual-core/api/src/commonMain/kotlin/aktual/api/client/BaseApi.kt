package aktual.api.client

import aktual.api.model.base.InfoResponse

interface BaseApi {
  suspend fun fetchInfo(): InfoResponse
}
