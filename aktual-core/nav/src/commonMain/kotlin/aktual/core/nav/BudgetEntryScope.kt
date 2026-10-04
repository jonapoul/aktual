package aktual.core.nav

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope

/**
 * Registers the entries shown in [tab]'s stack. Any route can be pushed onto any tab's stack (e.g.
 * an account's transactions from Home), so entries are keyed by the tab hosting them rather than
 * the one their route belongs to.
 */
class BudgetEntryScope(
  @PublishedApi internal val tab: BudgetTab,
  @PublishedApi internal val scope: EntryProviderScope<BudgetNavKey>,
) {
  inline fun <reified K : BudgetNavKey> budgetEntry(
    metadata: Map<String, Any> = emptyMap(),
    noinline content: @Composable (K) -> Unit,
  ) =
    scope.entry<K>(
      clazzContentKey = { key -> budgetContentKey(tab, key) },
      metadata = metadata,
      content = content,
    )
}

/**
 * Unique per entry and hosting tab, so each one gets its own saveable state and ViewModelStore, and
 * prefixed with that tab so transitions can tell which tab an entry is shown in via [budgetTabOf].
 * A String because saveable state keys must be saveable on Android.
 */
fun budgetContentKey(tab: BudgetTab, key: BudgetNavKey): String = "${tab.name}$SEPARATOR$key"

fun budgetTabOf(contentKey: Any): BudgetTab =
  BudgetTab.valueOf(contentKey.toString().substringBefore(SEPARATOR))

private const val SEPARATOR = "/"
