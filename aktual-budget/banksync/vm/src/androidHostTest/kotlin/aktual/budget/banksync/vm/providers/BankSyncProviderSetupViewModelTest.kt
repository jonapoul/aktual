package aktual.budget.banksync.vm.providers

import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.SecretResponse
import aktual.api.model.banksync.SecretResponse.Failed
import aktual.budget.banksync.domain.BankSyncProviderSetup
import aktual.budget.banksync.domain.ProviderCredential.EnableBankingApplicationId
import aktual.budget.banksync.domain.ProviderCredential.EnableBankingSecretKey
import aktual.budget.banksync.domain.ProviderCredential.SimpleFinToken
import aktual.budget.banksync.vm.FakeEnableBankingApi
import aktual.budget.banksync.vm.FakeSecretsApi
import aktual.budget.banksync.vm.providers.SetupError.Other
import aktual.budget.model.AccountSyncSource
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.AfterTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test

class BankSyncProviderSetupViewModelTest {
  private val secrets = FakeSecretsApi()
  private val enableBanking = FakeEnableBankingApi()

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Saving needs every credential`() = runTest {
    val viewModel = createViewModel(EnableBanking)

    viewModel.state.test {
      assertThat(awaitItem())
        .isEqualTo(
          BankSyncProviderSetupState(
            source = EnableBanking,
            fields =
              persistentListOf(
                SetupField(EnableBankingApplicationId),
                SetupField(EnableBankingSecretKey),
              ),
            redirectUrl = "https://test.server.com/enablebanking/auth_callback",
          )
        )
      viewModel.setValue(EnableBankingApplicationId, "app")
      assertThat(awaitItem().canSave).isFalse()
      viewModel.setValue(EnableBankingSecretKey, "pem")
      assertThat(awaitItem().canSave).isTrue()
    }
  }

  @Test
  fun `Saves the credentials`() = runTest {
    val viewModel = createViewModel(SimpleFin)
    viewModel.setValue(SimpleFinToken, "token")

    viewModel.events.test {
      viewModel.save()
      assertThat(awaitItem()).isEqualTo(Saved)
    }
    assertThat(secrets.set)
      .containsExactly(
        BankSyncSecret.SimpleFinToken to "token",
        BankSyncSecret.SimpleFinAccessKey to null,
      )
    assertThat(viewModel.state.value.redirectUrl).isNull()
  }

  @Test
  fun `Shows why Enable Banking rejected the credentials`() = runTest {
    enableBanking.configureResponse = Failed("CONFIGURATION_FAILED", "Invalid application ID")
    val viewModel = createViewModel(EnableBanking)
    viewModel.setValue(EnableBankingApplicationId, "app")
    viewModel.setValue(EnableBankingSecretKey, "pem")

    viewModel.state.test {
      skipItems(1)
      viewModel.save()
      assertThat(awaitItem().isSaving).isTrue()
      val failed = awaitItem()
      assertThat(failed.isSaving).isFalse()
      assertThat(failed.error).isEqualTo(Other("Invalid application ID"))
    }
    assertThat(enableBanking.configured).containsExactly("app" to "pem")
    assertThat(secrets.set).isEmpty()
  }

  @Test
  fun `Only admins can save`() = runTest {
    secrets.response = Failed(SecretResponse.NOT_ADMIN, details = "Admins only")
    val viewModel = createViewModel(SimpleFin)
    viewModel.setValue(SimpleFinToken, "token")

    viewModel.state.test {
      skipItems(1)
      viewModel.save()
      skipItems(1)
      assertThat(awaitItem().error).isEqualTo(NotAdmin)
      // Editing clears the error
      viewModel.setValue(SimpleFinToken, "other")
      assertThat(awaitItem().error).isNull()
    }
  }

  private fun TestScope.createViewModel(source: AccountSyncSource): BankSyncProviderSetupViewModel {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    return BankSyncProviderSetupViewModel(
      source = source,
      setup = BankSyncProviderSetup(secrets, enableBanking),
      server = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("token")),
    )
  }
}
