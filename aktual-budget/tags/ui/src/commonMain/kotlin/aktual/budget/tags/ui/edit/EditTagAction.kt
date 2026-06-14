package aktual.budget.tags.ui.edit

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable internal sealed interface EditTagAction

internal data object NavigateBack : EditTagAction

internal data object SaveTag : EditTagAction

internal data object DismissSaveError : EditTagAction

@JvmInline internal value class SetTag(val tag: String) : EditTagAction

@JvmInline internal value class SetDescription(val description: String) : EditTagAction

@JvmInline internal value class SetColor(val color: Color) : EditTagAction

@JvmInline internal value class SetColorError(val isError: Boolean) : EditTagAction

@Immutable
internal fun interface EditTagActionHandler {
  operator fun invoke(action: EditTagAction)
}
