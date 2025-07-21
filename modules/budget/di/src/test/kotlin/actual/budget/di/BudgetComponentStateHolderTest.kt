package actual.budget.di

import actual.budget.db.withResult
import actual.budget.db.withoutResult
import actual.budget.model.AndroidBudgetFiles
import actual.budget.model.BankId
import actual.budget.model.DbMetadata
import alakazam.kotlin.core.requireMessage
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.assertEmpty
import alakazam.test.core.standardDispatcher
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
class BudgetComponentStateHolderTest {
  private lateinit var stateHolder: BudgetComponentStateHolder

  private fun TestScope.before() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fileSystem = FileSystem.SYSTEM
    val files = AndroidBudgetFiles(context, fileSystem)
    val contexts = TestCoroutineContexts(standardDispatcher)
    stateHolder = BudgetComponentStateHolder(context, files, this, contexts)
  }

  @AfterTest
  fun after() {
    stateHolder.close()
  }

  @Test
  fun `Close database when updating`() = runTest {
    before()

    val bankId = BankId("my-bank-id")
    val metadata1 = metadata(id = "abc-123")

    // create database
    val component1 = stateHolder.update(metadata1)

    // insert data and validate
    assertEmpty(component1.fetchData(bankId))
    component1.insertData(bankId)
    assertEquals(expected = 1, actual = component1.fetchData(bankId).size)

    // target a different database
    val metadata2 = metadata(id = "xyz-789")
    val component2 = stateHolder.update(metadata2)

    // insert data separately and re-validate
    assertEmpty(component2.fetchData(bankId))
    component2.insertData(bankId)
    assertEquals(expected = 1, actual = component2.fetchData(bankId).size)

    // try to access the first db, but it's closed
    val e = assertFailsWith<IllegalStateException> { component1.fetchData(bankId) }
    assertContains(e.requireMessage(), "attempt to re-open an already-closed object")

    // close the second one
    stateHolder.close()

    // and accessing it fails
    assertFailsWith<IllegalStateException> { component2.fetchData(bankId) }
  }

  private fun metadata(id: String) = DbMetadata(
    data = persistentMapOf(
      "cloudFileId" to id,
    ),
  )

  private suspend fun BudgetComponent.fetchData(bankId: BankId) = database
    .banksQueries
    .withResult { getByBankId(bankId).executeAsList() }

  private suspend fun BudgetComponent.insertData(
    bankId: BankId,
    id: Uuid = Uuid.random(),
    name: String = "my bank name",
  ) = database
    .banksQueries
    .withoutResult { insert(id, bankId, name) }
}
