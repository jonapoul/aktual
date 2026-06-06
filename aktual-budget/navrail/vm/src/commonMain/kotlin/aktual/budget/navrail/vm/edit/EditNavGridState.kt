package aktual.budget.navrail.vm.edit

import aktual.core.nav.BudgetTab
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class EditNavGridState(
  val items: ImmutableList<BudgetTab>,
  val hasChanges: Boolean,
  val isDefault: Boolean,
)
