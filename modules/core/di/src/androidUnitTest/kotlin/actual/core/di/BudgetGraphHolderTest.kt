package actual.core.di

import actual.app.di.AndroidViewModelGraph
import actual.app.di.CoroutineContainer
import actual.app.di.GithubApiContainer
import actual.budget.db.withResult
import actual.budget.db.withoutResult
import actual.budget.model.BankId
import actual.budget.model.DbMetadata
import actual.test.DummyViewModelAssistedFactoryContainer
import actual.test.DummyViewModelContainer
import actual.test.messageContains
import alakazam.kotlin.core.CoroutineContexts
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.standardDispatcher
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isInstanceOf
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
class BudgetGraphHolderTest : AppGraph.Holder {
  private lateinit var appGraph: TestAppGraph
  private lateinit var holder: BudgetGraphHolder

  override fun invoke(): AppGraph = appGraph

  private fun TestScope.before() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val contexts = TestCoroutineContexts(standardDispatcher)
    val appGraphHolder = this@BudgetGraphHolderTest
    appGraph = createGraphFactory<TestAppGraph.Factory>().create(
      scope = this,
      contexts = contexts,
      context = context,
      holder = appGraphHolder,
    )

    holder = appGraph.budgetGraphHolder
  }

  @AfterTest
  fun after() {
    holder.close()
  }

  @Test
  fun `Close database when updating`() = runTest {
    before()

    val bankId = BankId("my-bank-id")
    val metadata1 = metadata(id = "abc-123")

    // create database
    val graph1 = holder.update(metadata1)

    // insert data and validate
    assertThat(graph1.fetchData(bankId)).isEmpty()
    graph1.insertData(bankId)
    assertThat(graph1.fetchData(bankId)).hasSize(1)

    // target a different database
    val metadata2 = metadata(id = "xyz-789")
    val graph2 = holder.update(metadata2)

    // insert data separately and re-validate
    assertThat(graph2.fetchData(bankId)).isEmpty()
    graph2.insertData(bankId)
    assertThat(graph2.fetchData(bankId)).hasSize(1)

    // try to access the first db, but it's closed
    assertFailure { graph1.fetchData(bankId) }
      .isInstanceOf<IllegalStateException>()
      .messageContains("attempt to re-open an already-closed object")

    // close the second one
    holder.close()

    // and accessing it fails
    assertFailure { graph2.fetchData(bankId) }.isInstanceOf<IllegalStateException>()
  }

  private fun metadata(id: String) = DbMetadata(
    data = persistentMapOf(
      "cloudFileId" to id,
    ),
  )

  private suspend fun BudgetGraph.fetchData(bankId: BankId) = database
    .banksQueries
    .withResult { getByBankId(bankId).executeAsList() }

  private suspend fun BudgetGraph.insertData(
    bankId: BankId,
    id: Uuid = Uuid.random(),
    name: String = "my bank name",
  ) = database
    .banksQueries
    .withoutResult { insert(id, bankId, name) }

  @DependencyGraph(
    scope = AppScope::class,
    excludes = [CoroutineContainer::class, GithubApiContainer::class],
    bindingContainers = [DummyViewModelContainer::class, DummyViewModelAssistedFactoryContainer::class],
  )
  internal interface TestAppGraph : AppGraph, AndroidViewModelGraph.Factory {
    val budgetGraphHolder: BudgetGraphHolder

    @DependencyGraph.Factory
    fun interface Factory {
      fun create(
        @Provides scope: CoroutineScope,
        @Provides contexts: CoroutineContexts,
        @Provides context: Context,
        @Provides holder: AppGraph.Holder,
      ): TestAppGraph
    }
  }
}
