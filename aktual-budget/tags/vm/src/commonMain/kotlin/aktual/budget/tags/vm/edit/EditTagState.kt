package aktual.budget.tags.vm.edit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
sealed interface EditTagState {
  data object Loading : EditTagState

  data class Editing(
    val initialTag: String = "",
    val initialDescription: String = "",
    val color: Color? = null,
    val isNew: Boolean = true,
  ) : EditTagState

  @JvmInline value class Failure(val cause: String?) : EditTagState
}
