package aktual.budget.banksync.vm.providers

import aktual.api.model.banksync.BankSyncSecret
import aktual.api.model.banksync.BankSyncStatusResponse.Success
import aktual.api.model.banksync.SecretResponse
import aktual.budget.banksync.domain.BankSyncProviderSetup
import aktual.budget.banksync.vm.BankSyncProviderStatus
import aktual.budget.banksync.vm.FakeBankSyncApi
import aktual.budget.banksync.vm.FakeEnableBankingApi
import aktual.budget.banksync.vm.FakeSecretsApi
import aktual.budget.banksync.vm.providers.BankSyncProvidersEvent.ResetFailed
import aktual.budget.model.AccountSyncSource
import aktual.core.model.BudgetServer
import aktual.core.model.Protocol
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BankSyncProvidersViewModelTest {
  private val secrets = FakeSecretsApi()

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Lists each provider's status`() = runTest {
    val viewModel = createViewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(loaded(Checking))
      testScheduler.advanceUntilIdle()
      assertThat(expectMostRecentItem())
        .isEqualTo(
          BankSyncProvidersState.Loaded(
            persistentListOf(
              BankSyncProviderItem(GoCardless, Configured),
              BankSyncProviderItem(EnableBanking, NotConfigured),
              BankSyncProviderItem(SimpleFin, NotConfigured),
              BankSyncProviderItem(PluggyAi, NotConfigured),
              BankSyncProviderItem(Akahu, NotConfigured),
            )
          )
        )
    }
  }

  @Test
  fun `Nothing to set up without a server`() = runTest {
    createViewModel(None).state.test {
      assertThat(awaitItem()).isEqualTo(NoServer)
    }
  }

  @Test
  fun `Resetting clears the provider's secrets`() = runTest {
    val viewModel = createViewModel()

    viewModel.events.test {
      testScheduler.advanceUntilIdle()
      viewModel.reset(GoCardless)
      assertThat(awaitItem()).isEqualTo(Reset)
    }
    assertThat(secrets.set)
      .containsExactly(
        BankSyncSecret.GoCardlessSecretId to null,
        BankSyncSecret.GoCardlessSecretKey to null,
      )
    testScheduler.advanceUntilIdle()
    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(loaded(NotConfigured))
    }
  }

  @Test
  fun `Only admins can reset`() = runTest {
    secrets.response = SecretResponse.Failed(SecretResponse.NOT_ADMIN, details = "Admins only")
    val viewModel = createViewModel()

    viewModel.events.test {
      testScheduler.advanceUntilIdle()
      viewModel.reset(GoCardless)
      assertThat(awaitItem()).isEqualTo(ResetFailed(NotAdmin))
    }
  }

  private fun loaded(goCardless: BankSyncProviderStatus) =
    BankSyncProvidersState.Loaded(
      persistentListOf(
        BankSyncProviderItem(GoCardless, goCardless),
        BankSyncProviderItem(EnableBanking, otherStatus(goCardless)),
        BankSyncProviderItem(SimpleFin, otherStatus(goCardless)),
        BankSyncProviderItem(PluggyAi, otherStatus(goCardless)),
        BankSyncProviderItem(Akahu, otherStatus(goCardless)),
      )
    )

  // Only GoCardless is configured, so the rest stay unconfigured once checked
  private fun otherStatus(goCardless: BankSyncProviderStatus): BankSyncProviderStatus =
    if (goCardless == Checking) Checking else NotConfigured

  private fun TestScope.createViewModel(server: BudgetServer = SERVER): BankSyncProvidersViewModel {
    Dispatchers.setMain(StandardTestDispatcher(testScheduler))
    val configured = Success(configured = true)
    val unconfigured = Success(configured = false)
    return BankSyncProvidersViewModel(
      api =
        FakeBankSyncApi(
          AccountSyncSource.GoCardless to configured,
          AccountSyncSource.EnableBanking to unconfigured,
          AccountSyncSource.SimpleFin to unconfigured,
          AccountSyncSource.PluggyAi to unconfigured,
          AccountSyncSource.Akahu to unconfigured,
        ),
      setup = BankSyncProviderSetup(secrets, FakeEnableBankingApi()),
      server = server,
    )
  }

  private companion object {
    val SERVER = BudgetServer.Remote(ServerUrl(Protocol.Https, "test.server.com"), Token("token"))
  }
}
