package aktual.budget.transactions.vm.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.model.ScheduleId
import aktual.budget.model.TransactionId
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.domain.TransactionDraft
import aktual.budget.transactions.domain.TransactionField
import aktual.budget.transactions.domain.TransactionFields
import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import logcat.logcat

// The editor's draft as it's kept in the saved state, so it outlives the process
@Serializable
internal data class EditorSnapshot(
  // Null while there's no draft open
  val draft: DraftSnapshot? = null,
  // What the draft started from: what was saved when it was opened, or where a new transaction
  // started
  val base: DraftSnapshot? = null,
  val newPayees: List<NewPayeeSnapshot> = emptyList(),
  val emptied: Set<TransactionField> = emptySet(),
  val hasConflicts: Boolean = false,
  // An amount typed that rules haven't run on yet
  val hasUncommittedAmount: Boolean = false,
)

@Serializable internal data class NewPayeeSnapshot(val id: PayeeId, val name: String)

@Serializable
internal data class DraftSnapshot(
  val account: AccountId?,
  val date: LocalDate,
  val amount: Long,
  val payee: PayeeId?,
  val category: CategoryId?,
  val notes: String?,
  val cleared: Boolean,
  val reconciled: Boolean,
  val schedule: ScheduleId? = null,
  val parts: List<PartSnapshot> = emptyList(),
)

@Serializable
internal data class PartSnapshot(
  val id: TransactionId,
  val amount: Long,
  val payee: PayeeId?,
  val category: CategoryId?,
  val notes: String?,
  val sortOrder: Long?,
)

internal fun TransactionDraft.toSnapshot() =
  DraftSnapshot(
    account = fields.account,
    date = fields.date,
    amount = fields.amount.toLong(),
    payee = fields.payee,
    category = fields.category,
    notes = fields.notes,
    cleared = fields.cleared,
    reconciled = fields.reconciled,
    schedule = fields.schedule,
    parts =
      parts.map { part ->
        PartSnapshot(
          id = part.id,
          amount = part.amount.toLong(),
          payee = part.payee,
          category = part.category,
          notes = part.notes,
          sortOrder = part.sortOrder,
        )
      },
  )

internal fun DraftSnapshot.toDraft() =
  TransactionDraft(
    fields =
      TransactionFields(
        account = account,
        date = date,
        amount = Amount(amount),
        payee = payee,
        category = category,
        notes = notes,
        cleared = cleared,
        reconciled = reconciled,
        schedule = schedule,
      ),
    parts =
      parts.map { part ->
        SplitPart(
          id = part.id,
          amount = Amount(part.amount),
          payee = part.payee,
          category = part.category,
          notes = part.notes,
          sortOrder = part.sortOrder,
        )
      },
  )

// Keeps whatever the snapshot is when the state's saved
internal fun SavedStateHandle.provideEditorSnapshot(snapshot: () -> EditorSnapshot) =
  setSavedStateProvider(KEY_SNAPSHOT) { encodeToSavedState(snapshot()) }

// Null if there's nothing kept, or it can't be read
internal fun SavedStateHandle.restoreEditorSnapshot(): EditorSnapshot? =
  try {
    get<SavedState>(KEY_SNAPSHOT)?.let { decodeFromSavedState<EditorSnapshot>(it) }
  } catch (e: IllegalArgumentException) {
    // Including a SerializationException
    logcat.w(e) { "Failed restoring a transaction draft" }
    null
  }

private const val KEY_SNAPSHOT = "draft"
