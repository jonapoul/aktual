package aktual.di

import kotlinx.coroutines.CoroutineScope

// Cancelled when the matching graph closes. Inject one of these rather than a plain CoroutineScope

interface AppCoroutineScope : CoroutineScope

interface ServerChosenCoroutineScope : CoroutineScope

interface LoggedInCoroutineScope : CoroutineScope

interface BudgetCoroutineScope : CoroutineScope

fun AppCoroutineScope(scope: CoroutineScope): AppCoroutineScope =
  object : AppCoroutineScope, CoroutineScope by scope {}

fun ServerChosenCoroutineScope(scope: CoroutineScope): ServerChosenCoroutineScope =
  object : ServerChosenCoroutineScope, CoroutineScope by scope {}

fun LoggedInCoroutineScope(scope: CoroutineScope): LoggedInCoroutineScope =
  object : LoggedInCoroutineScope, CoroutineScope by scope {}

fun BudgetCoroutineScope(scope: CoroutineScope): BudgetCoroutineScope =
  object : BudgetCoroutineScope, CoroutineScope by scope {}
