package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.UndoToken
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface CategoryTarget {
  val name: String

  data class Category(val id: CategoryId, override val name: String) : CategoryTarget

  data class Group(val id: CategoryGroupId, override val name: String) : CategoryTarget
}

enum class AddKind {
  NewCategory,
  NewGroup,
}

enum class DialogStatus {
  Editing,
  Saving,
  // The name is already used, so the dialog stays open
  Duplicate,
  Failed,
}

@Immutable
sealed interface CategoryDialog {
  val status: DialogStatus

  data class Add(
    val groups: ImmutableList<GroupChoice>,
    override val status: DialogStatus = DialogStatus.Editing,
  ) : CategoryDialog

  data class Rename(
    val target: CategoryTarget,
    override val status: DialogStatus = DialogStatus.Editing,
  ) : CategoryDialog

  data class Delete(
    val target: CategoryTarget,
    val transfer: Transfer,
    override val status: DialogStatus = DialogStatus.Editing,
  ) : CategoryDialog
}

@Immutable data class GroupChoice(val id: CategoryGroupId, val name: String, val isIncome: Boolean)

// Whether a deleted category's transactions and budgets need somewhere to go
@Immutable
sealed interface Transfer {
  data object Checking : Transfer

  data object NotNeeded : Transfer

  // The categories they can move to, under their groups
  @JvmInline value class Needed(val groups: ImmutableList<TransferGroup>) : Transfer
}

@Immutable
data class TransferGroup(val name: String, val categories: ImmutableList<TransferCategory>)

@Immutable data class TransferCategory(val id: CategoryId, val name: String)

sealed interface CategoryEditEvent {
  // A change the token undoes
  @JvmInline value class Updated(val token: UndoToken) : CategoryEditEvent

  @JvmInline value class Deleted(val token: UndoToken) : CategoryEditEvent
}
