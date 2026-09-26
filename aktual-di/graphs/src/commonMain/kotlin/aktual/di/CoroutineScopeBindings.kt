package aktual.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job

// Each scope's CoroutineScope is a child of its parent's, and is cancelled when its graph closes

@BindingContainer
@ContributesTo(AppScope::class)
object AppCoroutineBindings {
  @Provides
  @SingleIn(AppScope::class)
  fun scope(): AppCoroutineScope = AppCoroutineScope(MainScope())

  @Provides
  @IntoSet
  @ForScope(AppScope::class)
  fun closeable(scope: AppCoroutineScope): Closeable = Closeable { scope.cancel() }
}

@BindingContainer
@ContributesTo(ServerChosenScope::class)
object ServerChosenCoroutineBindings {
  @Provides
  @SingleIn(ServerChosenScope::class)
  fun scope(parent: AppCoroutineScope): ServerChosenCoroutineScope =
    ServerChosenCoroutineScope(parent.childScope())

  @Provides
  @IntoSet
  @ForScope(ServerChosenScope::class)
  fun closeable(scope: ServerChosenCoroutineScope): Closeable = Closeable { scope.cancel() }
}

@BindingContainer
@ContributesTo(LoggedInScope::class)
object LoggedInCoroutineBindings {
  @Provides
  @SingleIn(LoggedInScope::class)
  fun scope(parent: ServerChosenCoroutineScope): LoggedInCoroutineScope =
    LoggedInCoroutineScope(parent.childScope())

  @Provides
  @IntoSet
  @ForScope(LoggedInScope::class)
  fun closeable(scope: LoggedInCoroutineScope): Closeable = Closeable { scope.cancel() }
}

@BindingContainer
@ContributesTo(BudgetScope::class)
object BudgetCoroutineBindings {
  @Provides
  @SingleIn(BudgetScope::class)
  fun scope(parent: LoggedInCoroutineScope): BudgetCoroutineScope =
    BudgetCoroutineScope(parent.childScope())

  @Provides
  @IntoSet
  @ForScope(BudgetScope::class)
  fun closeable(scope: BudgetCoroutineScope): Closeable = Closeable { scope.cancel() }
}

private fun CoroutineScope.childScope(): CoroutineScope =
  CoroutineScope(coroutineContext + SupervisorJob(coroutineContext.job))
