package aktual.budget.banksync.domain

import aktual.api.client.SecretsApi
import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.SecretResponse

internal class FakeSecretsApi : SecretsApi {
  val set = mutableListOf<Pair<BankSyncSecret, String?>>()
  var response: SecretResponse = Success

  override suspend fun set(secret: BankSyncSecret, value: String?): SecretResponse {
    set += secret to value
    return response
  }
}
