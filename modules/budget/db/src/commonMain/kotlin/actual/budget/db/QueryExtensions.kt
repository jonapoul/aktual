package actual.budget.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.SuspendingTransacter
import app.cash.sqldelight.coroutines.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

suspend fun <T : SuspendingTransacter> T.withoutResult(
  query: suspend T.() -> Unit,
): Unit = transaction { query() }

suspend fun <T : SuspendingTransacter, R> T.withResult(
  query: suspend T.() -> R,
): R = transactionWithResult { query() }

fun <T : Any> Query<T>.asSingleFlow(): Flow<T> = asFlow().map { it.executeAsOne() }
fun <T : Any> Query<T>.asSingleNullableFlow(): Flow<T?> = asFlow().map { it.executeAsOneOrNull() }
fun <T : Any> Query<T>.asListFlow(): Flow<List<T>> = asFlow().map { it.executeAsList() }
