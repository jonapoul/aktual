package aktual.api.model.banksync

/**
 * A bank sync provider's credential, which the server keeps and only admins can set.
 *
 * See SecretName in packages/sync-server/src/services/secrets-service.ts
 */
enum class BankSyncSecret(val value: String) {
  GoCardlessSecretId("gocardless_secretId"),
  GoCardlessSecretKey("gocardless_secretKey"),
  SimpleFinToken("simplefin_token"),
  SimpleFinAccessKey("simplefin_accessKey"),
  PluggyAiClientId("pluggyai_clientId"),
  PluggyAiClientSecret("pluggyai_clientSecret"),
  PluggyAiItemIds("pluggyai_itemIds"),
  AkahuUserToken("akahu_userToken"),
  AkahuAppToken("akahu_appToken"),
  EnableBankingApplicationId("enablebanking_applicationId"),
  EnableBankingSecretKey("enablebanking_secretKey"),
}

sealed interface SecretResponse {
  data object Success : SecretResponse

  data class Failed(val reason: String?, val details: String?) : SecretResponse

  companion object {
    const val NOT_ADMIN = "not-admin"
    const val UNAUTHORIZED = "unauthorized"
  }
}
