package aktual.core.nav

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope

inline fun <reified K : BudgetNavKey> EntryProviderScope<BudgetNavKey>.budgetEntry(
  metadata: Map<String, Any> = emptyMap(),
  noinline content: @Composable (K) -> Unit,
) = entry<K>(clazzContentKey = { key -> key.tab }, metadata = metadata, content = content)
