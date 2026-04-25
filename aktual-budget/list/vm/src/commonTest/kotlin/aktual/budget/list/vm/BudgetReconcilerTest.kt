package aktual.budget.list.vm

import aktual.api.model.sync.UserFile
import aktual.budget.model.Budget
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.core.model.KeyId
import aktual.prefs.KeyPreferences
import aktual.test.CoTemporaryFolder
import aktual.test.testBudgetFiles
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class BudgetReconcilerTest {
  @InterceptTest val temporaryFolder = CoTemporaryFolder()

  private lateinit var reconciler: BudgetReconciler
  private lateinit var files: BudgetFiles
  private lateinit var keyPreferences: KeyPreferences

  private fun TestScope.before(hasKeys: Boolean = false) {
    files = testBudgetFiles(temporaryFolder)
    keyPreferences = mockk { coEvery { contains(any()) } returns hasKeys }
    reconciler =
      BudgetReconciler(
        files = files,
        keyPreferences = keyPreferences,
        contexts = TestCoroutineContexts(standardDispatcher),
      )
  }

  @Test
  fun `Server file with matching local groupId is Synced`() = runTest {
    before()
    seedLocal(BudgetId("a"), groupId = "g1", name = "Main")

    val result =
      reconciler.reconcile(remote = listOf(userFile(id = "a", groupId = "g1", name = "Main")))

    assertThat(result)
      .containsExactly(
        Budget.Synced(
          name = "Main",
          hasKey = false,
          encryptKeyId = null,
          cloudFileId = BudgetId("a"),
          groupId = "g1",
          owner = null,
        )
      )
  }

  @Test
  fun `Server file with mismatched local groupId is Detached`() = runTest {
    before()
    seedLocal(BudgetId("a"), groupId = "g1", name = "Main")

    val result =
      reconciler.reconcile(remote = listOf(userFile(id = "a", groupId = "g2", name = "Main")))

    assertThat(result)
      .containsExactly(
        Budget.Detached(
          name = "Main",
          hasKey = false,
          encryptKeyId = null,
          cloudFileId = BudgetId("a"),
          groupId = "g2",
          owner = null,
        )
      )
  }

  @Test
  fun `Server file with no local copy is Remote`() = runTest {
    before()

    val result =
      reconciler.reconcile(remote = listOf(userFile(id = "a", groupId = "g1", name = "Main")))

    assertThat(result)
      .containsExactly(
        Budget.Remote(
          name = "Main",
          hasKey = false,
          encryptKeyId = null,
          cloudFileId = BudgetId("a"),
          groupId = "g1",
          owner = null,
        )
      )
  }

  @Test
  fun `Local with cloud metadata but missing from server is Broken`() = runTest {
    before()
    seedLocal(BudgetId("a"), groupId = "g1", name = "Orphan")

    val result = reconciler.reconcile(remote = emptyList())

    assertThat(result)
      .containsExactly(
        Budget.Broken(
          name = "Orphan",
          hasKey = false,
          encryptKeyId = null,
          cloudFileId = BudgetId("a"),
          groupId = "g1",
        )
      )
  }

  @Test
  fun `Local without cloud metadata is Local`() = runTest {
    before()
    files.directory(BudgetId("a"), mkdirs = true)

    val result = reconciler.reconcile(remote = emptyList())

    assertThat(result)
      .containsExactly(
        Budget.Local(id = BudgetId("a"), name = "a", hasKey = false, encryptKeyId = null)
      )
  }

  @Test
  fun `Offline surfaces local cloud-tagged budgets as Unknown`() = runTest {
    before()
    seedLocal(BudgetId("a"), groupId = "g1", name = "Main")

    val result = reconciler.reconcile(remote = null)

    assertThat(result)
      .containsExactly(
        Budget.Unknown(
          name = "Main",
          hasKey = false,
          encryptKeyId = null,
          cloudFileId = BudgetId("a"),
          groupId = "g1",
        )
      )
  }

  @Test
  fun `Server file marked deleted is filtered out`() = runTest {
    before()

    val result =
      reconciler.reconcile(
        remote = listOf(userFile(id = "a", groupId = "g1", name = "Gone").copy(deleted = 1))
      )

    assertThat(result).isEmpty()
  }

  @Test
  fun `Encrypted file with stored key has hasKey=true`() = runTest {
    before(hasKeys = true)

    val result =
      reconciler.reconcile(
        remote =
          listOf(userFile(id = "a", groupId = "g1", name = "Main", encryptKeyId = KeyId("key-1")))
      )

    assertThat(result)
      .containsExactly(
        Budget.Remote(
          name = "Main",
          hasKey = true,
          encryptKeyId = "key-1",
          cloudFileId = BudgetId("a"),
          groupId = "g1",
          owner = null,
        )
      )
  }

  private fun seedLocal(id: BudgetId, groupId: String, name: String) {
    files.writeMetadata(DbMetadata(cloudFileId = id, groupId = groupId, budgetName = name))
  }

  private fun userFile(id: String, groupId: String, name: String, encryptKeyId: KeyId? = null) =
    UserFile(
      deleted = 0,
      fileId = BudgetId(id),
      groupId = groupId,
      name = name,
      encryptKeyId = encryptKeyId,
      owner = null,
    )
}
