package aktual.budget.tags.vm.edit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
sealed interface EditTagState {
  data object Loading : EditTagState

  @Immutable
  data class Editing(
    // seed values for the text fields, applied once when the form first composes
    val initialTag: String = "",
    val initialDescription: String = "",
    // the working colour selection, owned by the view model so it survives config changes
    val color: Color? = null,
    val isNew: Boolean = true,
  ) : EditTagState
}
