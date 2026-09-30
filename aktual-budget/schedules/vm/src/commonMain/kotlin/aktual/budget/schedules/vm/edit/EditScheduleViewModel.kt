package aktual.budget.schedules.vm.edit

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.RulesDao
import aktual.budget.db.dao.ScheduleDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleAction
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.upcomingDates
import aktual.budget.schedules.vm.ScheduleStatus
import aktual.budget.schedules.vm.SchedulesLoader
import aktual.budget.schedules.vm.edit.EditScheduleState.Failure
import aktual.core.Calendar
import aktual.di.BudgetScope
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import logcat.logcat

@Stable
@AssistedInject
class EditScheduleViewModel
internal constructor(
  @Assisted private val scheduleId: ScheduleId?,
  private val scheduleDao: ScheduleDao,
  private val rulesDao: RulesDao,
  private val payeeDao: PayeeDao,
  private val accountDao: AccountDao,
  private val loader: SchedulesLoader,
  private val writer: ScheduleWriter,
  calendar: Calendar,
) : ViewModel() {
  private val today = calendar.today()

  // What's currently saved, so edits can be compared against it and thrown away
  private val mutableSaved = MutableStateFlow<Saved?>(null)
  private val mutableEntities = MutableStateFlow<Entities?>(null)
  private val mutableFailure = MutableStateFlow<Failure?>(null)
  private val mutableForm = MutableStateFlow<ScheduleForm?>(null)
  private val mutableIsEditing = MutableStateFlow(scheduleId == null)
  private val mutableIsWorking = MutableStateFlow(false)
  private val mutableError = MutableStateFlow<EditScheduleError?>(null)

  private val mutableEvents =
    MutableSharedFlow<EditScheduleEvent>(
      replay = 0,
      extraBufferCapacity = 1,
      onBufferOverflow = DROP_OLDEST,
    )
  val events: SharedFlow<EditScheduleEvent> = mutableEvents.asSharedFlow()

  // The reason the last save or delete failed, shown in a dialog
  val error: StateFlow<EditScheduleError?> = mutableError.asStateFlow()

  val state: StateFlow<EditScheduleState> =
    viewModelScope.launchMolecule(Immediate) {
      val saved by mutableSaved.collectAsState()
      val entities by mutableEntities.collectAsState()
      val failure by mutableFailure.collectAsState()
      val form by mutableForm.collectAsState()
      val isEditing by mutableIsEditing.collectAsState()
      val isWorking by mutableIsWorking.collectAsState()
      buildState(saved, entities, failure, form, isEditing, isWorking)
    }

  init {
    viewModelScope.launch { load() }
  }

  private fun buildState(
    saved: Saved?,
    entities: Entities?,
    failure: Failure?,
    form: ScheduleForm?,
    isEditing: Boolean,
    isWorking: Boolean,
  ): EditScheduleState {
    if (failure != null) return failure
    if (saved == null || entities == null || form == null) return Loading
    return EditScheduleState.Loaded(
      form = form,
      isNew = scheduleId == null,
      isEditing = isEditing,
      hasChanges = form != saved.form,
      isWorking = isWorking,
      status = saved.status,
      payeeName = form.payee?.let(entities.payeeNames::get),
      accountName = form.account?.let(entities.accountNames::get),
      payees = entities.payees,
      accounts = entities.accounts,
      upcomingDates = form.date.upcoming(),
    )
  }

  private fun ScheduleDate.upcoming(): ImmutableList<LocalDate> =
    when (this) {
      is ScheduleDate.Once -> persistentListOf(date)
      is ScheduleDate.Recurring ->
        config.upcomingDates(from = today, count = UPCOMING_COUNT).toImmutableList()
    }

  private suspend fun load() {
    try {
      mutableEntities.update { loadEntities() }
      val saved = loadSaved()
      if (saved == null) {
        mutableFailure.update { Failure.NotFound }
      } else {
        mutableSaved.update { saved }
        mutableForm.update { saved.form }
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed loading schedule $scheduleId" }
      mutableFailure.update { Failure.Other(e.requireMessage()) }
    }
  }

  private suspend fun loadEntities(): Entities {
    val payees =
      payeeDao
        .getAllActive()
        .mapNotNull { row -> row.name?.let { NamedEntity(row.id, it) } }
        .toImmutableList()
    val accounts =
      accountDao
        .getAllActive()
        .mapNotNull { row -> row.name?.let { NamedEntity(row.id, it) } }
        .toImmutableList()

    // Closed accounts aren't offered, but a schedule might still point at one
    val accountNames = accountDao.nameMap().mapNotNull { (id, name) -> name?.let { id to it } }
    return Entities(
      payees = payees,
      accounts = accounts,
      payeeNames = payees.associate { it.id to it.name },
      accountNames = accountNames.toMap(),
    )
  }

  private suspend fun loadSaved(): Saved? {
    if (scheduleId == null) {
      val form =
        ScheduleForm(
          name = "",
          payee = null,
          account = mutableEntities.value?.accounts?.singleOrNull()?.id,
          amount = ScheduleAmount.Approximately(Amount.Zero),
          date = ScheduleDate.Recurring(defaultRecurConfig(start = today)),
          postsTransaction = false,
        )
      return Saved(
        form,
        ruleId = null,
        conditions = emptyList(),
        actions = emptyList(),
        status = null,
      )
    }

    val row = scheduleDao[scheduleId] ?: return null
    val rule = row.rule?.let { rulesDao[it] }
    val conditions = rule?.conditions ?: row._conditions.orEmpty()
    val form =
      conditions.toForm(
        name = row.name,
        postsTransaction = row.posts_transaction == true,
        mappedPayee = row._payee,
        today = today,
      )
    return Saved(
      form = form,
      ruleId = row.rule,
      conditions = conditions,
      actions = rule?.actions.orEmpty(),
      status = loader.load(scheduleId)?.status,
    )
  }

  fun startEditing() = mutableIsEditing.update { true }

  // Throws away any edits
  fun stopEditing() {
    mutableForm.update { mutableSaved.value?.form ?: it }
    mutableIsEditing.update { false }
  }

  fun setName(name: String) = updateForm { it.copy(name = name) }

  fun setPayee(payee: PayeeId) = updateForm { it.copy(payee = payee) }

  fun setAccount(account: AccountId) = updateForm { it.copy(account = account) }

  fun setAmount(amount: ScheduleAmount) = updateForm { it.copy(amount = amount) }

  fun setDate(date: ScheduleDate) = updateForm { it.copy(date = date) }

  fun setPostsTransaction(posts: Boolean) = updateForm { it.copy(postsTransaction = posts) }

  fun dismissError() = mutableError.update { null }

  private fun updateForm(makeCopy: (ScheduleForm) -> ScheduleForm) = mutableForm.update { form ->
    form?.let(makeCopy)
  }

  fun save() {
    val loaded = state.value as? Loaded ?: return
    val saved = mutableSaved.value ?: return
    if (!loaded.canSave) return

    mutableIsWorking.update { true }
    viewModelScope.launch {
      try {
        if (scheduleId == null) {
          writer.create(loaded.form)
          mutableEvents.tryEmit(Created)
        } else {
          val ruleId = saved.ruleId ?: error("Schedule $scheduleId has no rule")
          writer.update(scheduleId, ruleId, saved.conditions, saved.actions, loaded.form)
          loadSaved()?.let { reloaded ->
            mutableSaved.update { reloaded }
            mutableForm.update { reloaded.form }
          }
          mutableIsEditing.update { false }
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: DuplicateScheduleNameException) {
        logcat.w(e) { "Duplicate name saving $scheduleId" }
        mutableError.update { EditScheduleError.DuplicateName(loaded.form.name.trim()) }
      } catch (e: Exception) {
        logcat.e(e) { "Failed saving $scheduleId" }
        mutableError.update { EditScheduleError.Saving(e.requireMessage()) }
      } finally {
        mutableIsWorking.update { false }
      }
    }
  }

  fun delete() {
    val id = scheduleId ?: return
    val saved = mutableSaved.value ?: return

    mutableIsWorking.update { true }
    viewModelScope.launch {
      try {
        writer.delete(id, saved.ruleId)
        mutableEvents.tryEmit(Deleted)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        logcat.e(e) { "Failed deleting $id" }
        mutableError.update { EditScheduleError.Deleting(e.requireMessage()) }
      } finally {
        mutableIsWorking.update { false }
      }
    }
  }

  private data class Saved(
    val form: ScheduleForm,
    val ruleId: RuleId?,
    val conditions: List<Condition>,
    val actions: List<RuleAction>,
    val status: ScheduleStatus?,
  )

  private data class Entities(
    val payees: ImmutableList<NamedEntity<PayeeId>>,
    val accounts: ImmutableList<NamedEntity<AccountId>>,
    val payeeNames: Map<PayeeId, String>,
    val accountNames: Map<AccountId, String>,
  )

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(scheduleId: ScheduleId?): EditScheduleViewModel
  }

  private companion object {
    const val UPCOMING_COUNT = 5
  }
}
