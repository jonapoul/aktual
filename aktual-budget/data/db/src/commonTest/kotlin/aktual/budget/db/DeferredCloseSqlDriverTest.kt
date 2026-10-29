package aktual.budget.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter.TransactionDispatcher
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class DeferredCloseSqlDriverTest {
  private val delegate = FakeDriver()
  private val driver = DeferredCloseSqlDriver(delegate)

  @Test
  fun closesImmediatelyWhenIdle() {
    driver.close()
    assertThat(delegate.closeCount).isEqualTo(1)
  }

  @Test
  fun closesOnlyOnce() {
    driver.close()
    driver.close()
    assertThat(delegate.closeCount).isEqualTo(1)
  }

  @Test
  fun waitsForCallInFlight() = runTest {
    val call = async { driver.execute(null, "SQL", 0).await() }
    runCurrent()

    driver.close()
    assertThat(delegate.closeCount).isEqualTo(0)

    delegate.gate.complete(Unit)
    assertThat(call.await()).isEqualTo(1L)
    assertThat(delegate.closeCount).isEqualTo(1)
  }

  @Test
  fun waitsForTransactionInFlight() = runTest {
    val transaction = async { driver.dispatch { delegate.gate.await() } }
    runCurrent()

    driver.close()
    assertThat(delegate.closeCount).isEqualTo(0)

    delegate.gate.complete(Unit)
    transaction.await()
    assertThat(delegate.dispatched).isTrue()
    assertThat(delegate.closeCount).isEqualTo(1)
  }

  @Test
  fun rejectsCallsAfterClose() = runTest {
    driver.close()
    assertFailure { driver.execute(null, "SQL", 0).await() }.isInstanceOf<IllegalStateException>()
    assertThat(delegate.executed).isFalse()
  }

  private class FakeDriver : SqlDriver, TransactionDispatcher {
    val gate = CompletableDeferred<Unit>()
    var closeCount = 0
    var executed = false
    var dispatched = false

    override fun execute(
      identifier: Int?,
      sql: String,
      parameters: Int,
      binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> = QueryResult.AsyncValue {
      executed = true
      gate.await()
      1L
    }

    override suspend fun <R> dispatch(transaction: suspend () -> R): R {
      dispatched = true
      return transaction()
    }

    override fun close() {
      closeCount++
    }

    override fun <R> executeQuery(
      identifier: Int?,
      sql: String,
      mapper: (SqlCursor) -> QueryResult<R>,
      parameters: Int,
      binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> = error("Unused")

    override fun newTransaction(): QueryResult<Transacter.Transaction> = error("Unused")

    override fun currentTransaction(): Transacter.Transaction? = null

    override fun addListener(vararg queryKeys: String, listener: Query.Listener) = Unit

    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) = Unit

    override fun notifyListeners(vararg queryKeys: String) = Unit
  }
}
