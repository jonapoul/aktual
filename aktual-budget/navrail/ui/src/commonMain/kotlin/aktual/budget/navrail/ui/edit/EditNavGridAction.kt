package aktual.budget.navrail.ui.edit

import androidx.compose.runtime.Immutable

internal sealed interface EditNavGridAction

internal data object NavBack : EditNavGridAction

internal data object Save : EditNavGridAction

internal data object ResetToSaved : EditNavGridAction

internal data object ResetToDefault : EditNavGridAction

internal data object ShowDiscardDialog : EditNavGridAction

internal data object DismissDiscardDialog : EditNavGridAction

internal data class Move(val from: Int, val to: Int) : EditNavGridAction

@Immutable
internal fun interface ActionHandler {
  operator fun invoke(action: EditNavGridAction)
}
