package aktual.budget.apikeys.vm

import aktual.api.model.apikeys.ApiKey
import aktual.api.model.apikeys.ApiKeyId
import aktual.api.model.apikeys.ApiKeysResponse
import aktual.api.model.apikeys.CreatedApiKey
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.core.model.Token
import aktual.test.TestBudgetLocalPreferences
import aktual.test.viewModelContext
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class ApiKeysViewModelTest {
  private val api = FakeApiKeysApi()

  @Test
  fun `Lists only this budget's keys`() = runTest {
    api.listResponse =
      ApiKeysResponse.Success(listOf(key(KEY_ID, BUDGET), key(ApiKeyId("other"), BudgetId("b2"))))
    val viewModel = createViewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(Loaded(persistentListOf(ITEM)))
    }
  }

  @Test
  fun `No keys`() = runTest {
    createViewModel().state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(Empty)
    }
  }

  @Test
  fun `Server without API keys`() = runTest {
    api.listResponse = ApiKeysResponse.Failed(ApiKeysResponse.UNSUPPORTED)
    createViewModel().state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(Unsupported)
    }
  }

  @Test
  fun `Budget not on a server`() = runTest {
    createViewModel(budget = null).state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(Unsupported)
    }
  }

  @Test
  fun `Failed to load`() = runTest {
    api.listResponse = ApiKeysResponse.Failed("unauthorized")
    createViewModel().state.test {
      assertThat(awaitItem()).isEqualTo(Loading)
      assertThat(awaitItem()).isEqualTo(Failure("unauthorized"))
    }
  }

  @Test
  fun `Creating a key shows it once`() = runTest {
    val viewModel = createViewModel()
    api.createResponse =
      ApiKeysResponse.Success(
        CreatedApiKey(KEY_ID, "Importer", listOf(BUDGET), CREATED_AT, Token("actual_abc")),
      )

    viewModel.dialog.test {
      assertThat(awaitItem()).isNull()
      viewModel.add()
      assertThat(awaitItem()).isEqualTo(CreateKey())
      viewModel.create(" Importer ")
      assertThat(awaitItem()).isEqualTo(CreateKey(isBusy = true))
      assertThat(awaitItem()).isEqualTo(CreatedKey(Token("actual_abc")))
    }
    assertThat(api.created).containsExactly("Importer" to listOf(BUDGET))
  }

  @Test
  fun `Failing to create a key keeps the dialog open`() = runTest {
    val viewModel = createViewModel()

    viewModel.dialog.test {
      assertThat(awaitItem()).isNull()
      viewModel.add()
      assertThat(awaitItem()).isEqualTo(CreateKey())
      viewModel.create("Importer")
      assertThat(awaitItem()).isEqualTo(CreateKey(isBusy = true))
      assertThat(awaitItem()).isEqualTo(CreateKey(hasFailed = true))
    }
  }

  @Test
  fun `Revoking a key`() = runTest {
    val viewModel = createViewModel()

    viewModel.dialog.test {
      assertThat(awaitItem()).isNull()
      viewModel.revoke(ITEM)
      assertThat(awaitItem()).isEqualTo(RevokeKey(ITEM))
      viewModel.confirmRevoke()
      assertThat(awaitItem()).isEqualTo(RevokeKey(ITEM, isBusy = true))
      assertThat(awaitItem()).isNull()
    }
    assertThat(api.revoked).containsExactly(KEY_ID)
  }

  @Test
  fun `Failing to revoke a key`() = runTest {
    api.revokeResponse = ApiKeysResponse.Failed(ApiKeysResponse.API_KEY_NOT_FOUND)
    val viewModel = createViewModel()

    viewModel.dialog.test {
      assertThat(awaitItem()).isNull()
      viewModel.revoke(ITEM)
      assertThat(awaitItem()).isEqualTo(RevokeKey(ITEM))
      viewModel.confirmRevoke()
      assertThat(awaitItem()).isEqualTo(RevokeKey(ITEM, isBusy = true))
      assertThat(awaitItem()).isEqualTo(RevokeKey(ITEM, hasFailed = true))
    }
  }

  private fun TestScope.createViewModel(budget: BudgetId? = BUDGET) =
    ApiKeysViewModel(
      api = api,
      localPreferences = TestBudgetLocalPreferences(DbMetadata().set(CloudFileId, budget)),
      timeZones = { TimeZone.UTC },
      viewModelContext = viewModelContext,
    )

  private fun key(id: ApiKeyId, budget: BudgetId) =
    ApiKey(id = id, name = "Importer", budgets = listOf(budget), createdAtSeconds = CREATED_AT)

  private companion object {
    val KEY_ID = ApiKeyId("k1")
    val BUDGET = BudgetId("b1")

    // 2026-11-11T00:00:00Z
    const val CREATED_AT = 1_794_355_200L
    val ITEM = ApiKeyItem(KEY_ID, "Importer", LocalDate(2026, 11, 11))
  }
}
