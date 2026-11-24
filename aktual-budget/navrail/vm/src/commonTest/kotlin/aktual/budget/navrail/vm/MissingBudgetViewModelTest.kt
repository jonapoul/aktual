package aktual.budget.navrail.vm

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.DbMetadataKey.Companion.EncryptKeyId
import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.DbMetadataKey.Companion.LastSyncedTimestamp
import aktual.budget.model.Timestamp
import aktual.budget.utils.SyncStateHolder
import aktual.budget.utils.UploadResult
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.TestBudgetLocalPreferences
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class MissingBudgetViewModelTest {
  private val result = CompletableDeferred<UploadResult>()
  private val syncStateHolder = SyncStateHolder()
  private val prefs =
    TestBudgetLocalPreferences(
      DbMetadata(
        cloudFileId = CLOUD_FILE_ID,
        groupId = "group",
        lastSyncedTimestamp = Timestamp.fromMilliseconds(1_700_000_000_000L),
      ),
    )

  // Like BudgetUploaderImpl, which picks a new ID when the budget has none
  private var uploadedId: BudgetId? = null

  @Test
  fun `Hidden until the budget goes missing`() = runTest {
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(Hidden)

      syncStateHolder.update { FileNotFound }
      assertThatNextEmissionIsEqualTo(MissingBudgetState.Shown(canReupload = true))
    }
  }

  @Test
  fun `Keeping it local forgets the server copy`() = runTest {
    syncStateHolder.update { FileNotFound }
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(MissingBudgetState.Shown(canReupload = true))

      viewModel.keepLocal()
      assertThatNextEmissionIsEqualTo(Hidden)
    }

    assertDetached()
    assertThat(syncStateHolder.value).isEqualTo(Inactive)
  }

  @Test
  fun `Uploads again under a new ID`() = runTest {
    syncStateHolder.update { FileNotFound }
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(MissingBudgetState.Shown(canReupload = true))

      viewModel.reupload()
      assertThatNextEmissionIsEqualTo(
        MissingBudgetState.Shown(canReupload = true, isUploading = true),
      )

      result.complete(Success)
      assertThatNextEmissionIsEqualTo(Hidden)
    }

    assertThat(uploadedId).isNotEqualTo(CLOUD_FILE_ID)
  }

  @Test
  fun `Shows a failed upload, then keeps it local`() = runTest {
    syncStateHolder.update { FileNotFound }
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(MissingBudgetState.Shown(canReupload = true))

      viewModel.reupload()
      assertThatNextEmissionIsEqualTo(
        MissingBudgetState.Shown(canReupload = true, isUploading = true),
      )

      result.complete(Unreachable)
      assertThatNextEmissionIsEqualTo(
        MissingBudgetState.Shown(canReupload = true, failure = Unreachable),
      )

      viewModel.keepLocal()
      assertThatNextEmissionIsEqualTo(Hidden)
    }

    assertDetached()
  }

  @Test
  fun `Encrypted budgets can't be uploaded again`() = runTest {
    prefs.update { it.set(EncryptKeyId, "key") }
    syncStateHolder.update { FileNotFound }
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(MissingBudgetState.Shown(canReupload = false))

      viewModel.reupload()
      expectNoEvents()
    }

    assertThat(prefs[CloudFileId]).isEqualTo(CLOUD_FILE_ID)
  }

  private fun assertDetached() {
    assertThat(prefs[CloudFileId]).isNull()
    assertThat(prefs[GroupId]).isNull()
    assertThat(prefs[LastSyncedTimestamp]).isNull()
  }

  private fun TestScope.viewModel() =
    MissingBudgetViewModel(
      uploader = {
        uploadedId = prefs[CloudFileId] ?: BudgetId("new-id")
        result.await()
      },
      localPreferences = prefs,
      syncStateHolder = syncStateHolder,
      server = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("token")),
      viewModelContext = viewModelContext,
    )

  private companion object {
    val CLOUD_FILE_ID = BudgetId("old-id")
  }
}
