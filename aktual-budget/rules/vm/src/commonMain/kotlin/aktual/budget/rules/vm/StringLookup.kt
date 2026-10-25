package aktual.budget.rules.vm

import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

fun interface StringLookup {
  suspend fun get(resource: StringResource): String
}

@ContributesBinding(BudgetScope::class)
class StringLookupImpl : StringLookup {
  override suspend fun get(resource: StringResource): String = getString(resource)
}
