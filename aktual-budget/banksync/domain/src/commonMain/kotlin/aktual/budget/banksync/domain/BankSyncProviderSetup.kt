package aktual.budget.banksync.domain

import aktual.api.client.EnableBankingApi
import aktual.api.client.SecretsApi
import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.SecretResponse
import aktual.budget.model.AccountSyncSource
import dev.zacsweers.metro.Inject

enum class ProviderCredential(val source: AccountSyncSource, internal val secret: BankSyncSecret) {
  GoCardlessSecretId(GoCardless, BankSyncSecret.GoCardlessSecretId),
  GoCardlessSecretKey(GoCardless, BankSyncSecret.GoCardlessSecretKey),
  SimpleFinToken(SimpleFin, BankSyncSecret.SimpleFinToken),
  PluggyAiClientId(PluggyAi, BankSyncSecret.PluggyAiClientId),
  PluggyAiClientSecret(PluggyAi, BankSyncSecret.PluggyAiClientSecret),
  PluggyAiItemIds(PluggyAi, BankSyncSecret.PluggyAiItemIds),
  AkahuAppToken(Akahu, BankSyncSecret.AkahuAppToken),
  AkahuUserToken(Akahu, BankSyncSecret.AkahuUserToken),
  EnableBankingApplicationId(
    EnableBanking,
    BankSyncSecret.EnableBankingApplicationId,
  ),
  EnableBankingSecretKey(EnableBanking, BankSyncSecret.EnableBankingSecretKey);

  companion object {
    fun of(source: AccountSyncSource): List<ProviderCredential> = entries.filter {
      it.source == source
    }
  }
}

/**
 * Saves the credentials the server uses to reach a bank sync provider, or clears them. They apply
 * to every budget on the server, so only admins can.
 *
 * See packages/desktop-client/src/components/modals/GoCardlessInitialiseModal.tsx and the other
 * providers' initialise modals
 */
@Inject
class BankSyncProviderSetup(
  private val secrets: SecretsApi,
  private val enableBanking: EnableBankingApi,
) {
  suspend fun save(
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

  suspend fun reset(source: AccountSyncSource): SecretResponse {
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
