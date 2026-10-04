package aktual.budget.banksync.domain

import aktual.api.client.EnableBankingApi
import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.BankSyncSecret.SimpleFinAccessKey
import aktual.api.model.banksync.EnableBankingAccountType
import aktual.api.model.banksync.EnableBankingAccountsResponse
import aktual.api.model.banksync.EnableBankingBank
import aktual.api.model.banksync.EnableBankingBanksResponse
import aktual.api.model.banksync.EnableBankingLoginResponse
import aktual.api.model.banksync.SecretResponse
import aktual.budget.banksync.domain.ProviderCredential.EnableBankingApplicationId
import aktual.budget.banksync.domain.ProviderCredential.EnableBankingSecretKey
import aktual.budget.banksync.domain.ProviderCredential.GoCardlessSecretId
import aktual.budget.banksync.domain.ProviderCredential.GoCardlessSecretKey
import aktual.budget.banksync.domain.ProviderCredential.SimpleFinToken
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

internal class BankSyncProviderSetupTest {
  private val secrets = FakeSecretsApi()
  private val enableBanking = FakeEnableBankingApi()
  private val setup = BankSyncProviderSetup(secrets, enableBanking)

  @Test
  fun `Saves each credential trimmed`() = runTest {
    val response =
      setup.save(
        GoCardless,
        mapOf(GoCardlessSecretId to " id ", GoCardlessSecretKey to "key\n"),
      )

    assertThat(response).isEqualTo(Success)
    assertThat(secrets.set)
      .containsExactly(
        BankSyncSecret.GoCardlessSecretId to "id",
        BankSyncSecret.GoCardlessSecretKey to "key",
      )
  }

  @Test
  fun `A new SimpleFIN token clears the access key it was claimed for`() = runTest {
    setup.save(SimpleFin, mapOf(SimpleFinToken to "token"))

    assertThat(secrets.set)
      .containsExactly(
        BankSyncSecret.SimpleFinToken to "token",
        SimpleFinAccessKey to null,
      )
  }

  @Test
  fun `Stops at the first refusal`() = runTest {
    val refused = SecretResponse.Failed(SecretResponse.NOT_ADMIN, details = null)
    secrets.response = refused

    val response =
      setup.save(
        GoCardless,
        mapOf(GoCardlessSecretId to "id", GoCardlessSecretKey to "key"),
      )

    assertThat(response).isEqualTo(refused)
    assertThat(secrets.set).containsExactly(BankSyncSecret.GoCardlessSecretId to "id")
  }

  @Test
  fun `Blank credentials aren't saved`() = runTest {
    assertFailure {
      setup.save(
        GoCardless,
        mapOf(GoCardlessSecretId to "id", GoCardlessSecretKey to " "),
      )
    }
      .isInstanceOf<IllegalArgumentException>()
    assertThat(secrets.set).isEmpty()
  }

  @Test
  fun `Enable Banking checks its credentials while saving them`() = runTest {
    val response =
      setup.save(
        EnableBanking,
        mapOf(EnableBankingApplicationId to "app", EnableBankingSecretKey to "pem\n"),
      )

    assertThat(response).isEqualTo(Success)
    assertThat(enableBanking.configured).containsExactly("app" to "pem")
    assertThat(secrets.set).isEmpty()
  }

  @Test
  fun `Reset clears every secret`() = runTest {
    setup.reset(SimpleFin)

    assertThat(secrets.set)
      .containsExactly(
        BankSyncSecret.SimpleFinToken to null,
        SimpleFinAccessKey to null,
      )
  }

  private class FakeEnableBankingApi : EnableBankingApi {
    val configured = mutableListOf<Pair<String, String>>()

    override suspend fun configure(applicationId: String, secretKey: String): SecretResponse {
      configured += applicationId to secretKey
      return SecretResponse.Success
    }

    override suspend fun banks(country: String): EnableBankingBanksResponse = error("Unused")

    override suspend fun login(
      bank: EnableBankingBank,
      type: EnableBankingAccountType,
    ): EnableBankingLoginResponse = error("Unused")

    override suspend fun accounts(state: String): EnableBankingAccountsResponse = error("Unused")
  }
}
