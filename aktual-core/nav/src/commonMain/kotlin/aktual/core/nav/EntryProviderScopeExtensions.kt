package aktual.core.nav

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope

inline fun <reified K : BudgetNavKey> EntryProviderScope<BudgetNavKey>.budgetEntry(
  metadata: Map<String, Any> = emptyMap(),
  noinline content: @Composable (K) -> Unit,
) = entry<K>(clazzContentKey = ::budgetContentKey, metadata = metadata, content = content)

/**
 * Unique per entry, so each one gets its own saveable state and ViewModelStore, but prefixed with
 * the tab so transitions can tell which tab an entry belongs to via [budgetTabOf]. A String because
 * saveable state keys must be saveable on Android.
 */
fun budgetContentKey(key: BudgetNavKey): String = "${key.tab.name}$SEPARATOR$key"

fun budgetTabOf(contentKey: Any): BudgetTab =
  BudgetTab.valueOf(contentKey.toString().substringBefore(SEPARATOR))

private const val SEPARATOR = "/"
