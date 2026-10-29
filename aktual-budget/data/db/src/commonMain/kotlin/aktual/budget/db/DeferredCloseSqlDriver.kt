package aktual.budget.db

import app.cash.sqldelight.SuspendingTransacter.TransactionDispatcher
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Closing the androidx driver finalizes cached statements on connections that other threads may
 * still be using, which crashes in native code. This rejects new calls once closed, and only closes
 * [delegate] when the last call in flight finishes.
 */
internal class DeferredCloseSqlDriver(private val delegate: SqlDriver) :
  SqlDriver by delegate, TransactionDispatcher {
  private val lock = ReentrantLock()
  private var inFlight = 0
  private var closed = false

  override fun <R> executeQuery(
    identifier: Int?,
    sql: String,
    mapper: (SqlCursor) -> QueryResult<R>,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<R> = QueryResult.AsyncValue {
    guard { delegate.executeQuery(identifier, sql, mapper, parameters, binders).await() }
  }

  override fun execute(
    identifier: Int?,
    sql: String,
    parameters: Int,
    binders: (SqlPreparedStatement.() -> Unit)?,
  ): QueryResult<Long> = QueryResult.AsyncValue {
    guard { delegate.execute(identifier, sql, parameters, binders).await() }
  }

  override fun newTransaction(): QueryResult<Transacter.Transaction> = QueryResult.AsyncValue {
    guard { delegate.newTransaction().await() }
  }

  override suspend fun <R> dispatch(transaction: suspend () -> R): R = guard {
    when (delegate) {
      is TransactionDispatcher -> delegate.dispatch(transaction)
      else -> transaction()
    }
  }

  override fun close() {
    val closeNow = lock.withLock {
      val wasOpen = !closed
      closed = true
      wasOpen && inFlight == 0
    }
    if (closeNow) delegate.close()
  }

  private suspend fun <T> guard(block: suspend () -> T): T {
    lock.withLock {
      check(!closed) { "Database is closed" }
      inFlight++
    }
    try {
      return block()
    } finally {
      val closeNow = lock.withLock { --inFlight == 0 && closed }
      if (closeNow) delegate.close()
    }
  }
}
