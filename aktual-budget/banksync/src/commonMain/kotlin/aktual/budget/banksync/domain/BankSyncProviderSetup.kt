package aktual.budget.banksync.domain

import aktual.api.model.banksync.SecretResponse
import aktual.budget.model.AccountSyncSource

enum class ProviderCredential(val source: AccountSyncSource) {
  GoCardlessSecretId(GoCardless),
  GoCardlessSecretKey(GoCardless),
  SimpleFinToken(SimpleFin),
  PluggyAiClientId(PluggyAi),
  PluggyAiClientSecret(PluggyAi),
  PluggyAiItemIds(PluggyAi),
  AkahuAppToken(Akahu),
  AkahuUserToken(Akahu),
  EnableBankingApplicationId(EnableBanking),
  EnableBankingSecretKey(EnableBanking);

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
interface BankSyncProviderSetup {
  suspend fun save(
    source: AccountSyncSource,
    values: Map<ProviderCredential, String>,
  ): SecretResponse

  suspend fun reset(source: AccountSyncSource): SecretResponse
}
