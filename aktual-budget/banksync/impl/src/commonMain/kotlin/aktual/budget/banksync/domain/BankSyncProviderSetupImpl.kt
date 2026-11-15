package aktual.budget.banksync.domain

import aktual.api.client.EnableBankingApi
import aktual.api.client.SecretsApi
import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.SecretResponse
import aktual.budget.model.AccountSyncSource
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(BudgetScope::class)
class BankSyncProviderSetupImpl(
  private val secrets: SecretsApi,
  private val enableBanking: EnableBankingApi,
) : BankSyncProviderSetup {
  override suspend fun save(
    source: AccountSyncSource,
    values: Map<ProviderCredential, String>,
  ): SecretResponse {
    val trimmed =
      ProviderCredential.of(source).associateWith { credential ->
        val value = values[credential]?.trim()
        require(!value.isNullOrEmpty()) { "Missing $credential" }
        value
      }
    // Enable Banking checks the credentials before saving them
    if (source == EnableBanking) {
      return enableBanking.configure(
        applicationId = trimmed.getValue(EnableBankingApplicationId),
        secretKey = trimmed.getValue(EnableBankingSecretKey),
      )
    }
    val changes = trimmed.map { (credential, value) -> credential.secret to value }
    // A new token needs claiming again, which the access key came from
    val cleared = if (source == SimpleFin) SIMPLEFIN_DERIVED else emptyList()
    return setAll(changes + cleared.map { it to null })
  }

  override suspend fun reset(source: AccountSyncSource): SecretResponse {
    val derived = if (source == SimpleFin) SIMPLEFIN_DERIVED else emptyList()
    val all = ProviderCredential.of(source).map { it.secret } + derived
    return setAll(all.map { it to null })
  }

  private suspend fun setAll(changes: List<Pair<BankSyncSecret, String?>>): SecretResponse {
    for ((secret, value) in changes) {
      val response = secrets.set(secret, value)
      if (response is Failed) return response
    }
    return Success
  }

  private companion object {
    val SIMPLEFIN_DERIVED = listOf(BankSyncSecret.SimpleFinAccessKey)
  }
}

private val ProviderCredential.secret: BankSyncSecret
  get() =
    when (this) {
      GoCardlessSecretId -> BankSyncSecret.GoCardlessSecretId
      GoCardlessSecretKey -> BankSyncSecret.GoCardlessSecretKey
      SimpleFinToken -> BankSyncSecret.SimpleFinToken
      PluggyAiClientId -> BankSyncSecret.PluggyAiClientId
      PluggyAiClientSecret -> BankSyncSecret.PluggyAiClientSecret
      PluggyAiItemIds -> BankSyncSecret.PluggyAiItemIds
      AkahuAppToken -> BankSyncSecret.AkahuAppToken
      AkahuUserToken -> BankSyncSecret.AkahuUserToken
      EnableBankingApplicationId -> BankSyncSecret.EnableBankingApplicationId
      EnableBankingSecretKey -> BankSyncSecret.EnableBankingSecretKey
    }
