/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.transactions.vm

import aktual.budget.db.dao.AccountsDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TransactionsDao
import aktual.budget.db.transactions.GetById
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.SortDirection
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount.AllAccounts
import aktual.budget.transactions.vm.LoadedAccount.Loading
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.core.di.BudgetGraphHolder
import aktual.core.di.throwIfWrongBudget
import aktual.core.model.LoginToken
import alakazam.kotlin.core.CoroutineContexts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@AssistedInject
class TransactionsViewModel(
  @Suppress("unused") @Assisted private val token: LoginToken,
  @Assisted private val budgetId: BudgetId,
  @Assisted private val spec: TransactionsSpec,
  budgetGraphs: BudgetGraphHolder,
  contexts: CoroutineContexts,
) : ViewModel() {
  // data sources
  private val budgetGraph = budgetGraphs.require()
  private val accountsDao = AccountsDao(budgetGraph.database, contexts)
  private val transactionsDao = TransactionsDao(budgetGraph.database, contexts)
  private val prefs = budgetGraph.localPreferences
  private val syncedPrefs = PreferencesDao(budgetGraph.database, contexts)

  // local data
  private val mutableLoadedAccount = MutableStateFlow<LoadedAccount>(Loading)
  private val expandedGroups = MutableStateFlow(persistentMapOf<LocalDate, Boolean>())
  private val expandAllGroups = MutableStateFlow(false)
  private val checkedTransactionIds = MutableStateFlow(persistentMapOf<TransactionId, Boolean>())

  // API
  val loadedAccount: StateFlow<LoadedAccount> = mutableLoadedAccount.asStateFlow()

  val format: StateFlow<TransactionsFormat> = prefs
    .map { meta -> meta[TransactionFormatKey] ?: TransactionsFormat.Default }
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsFormat.Default)

  val sorting: StateFlow<TransactionsSorting> = prefs
    .map(::TransactionsSorting)
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsSorting.Default)

  val transactions: StateFlow<ImmutableList<DatedTransactions>> = combine(getIdsFlow(), sorting, ::Pair)
    .distinctUntilChanged()
    .map { (datedIds, sorting) -> toDatedTransactions(datedIds, sorting) }
    .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  init {
    budgetGraph.throwIfWrongBudget(budgetId)

    when (val s = spec.accountSpec) {
      is AccountSpec.AllAccounts -> mutableLoadedAccount.update { AllAccounts }
      is AccountSpec.SpecificAccount -> viewModelScope.launch {
        val account = accountsDao[s.id] ?: error("No account matching $s")
        mutableLoadedAccount.update { SpecificAccount(account) }
      }
    }
  }

  fun setFormat(format: TransactionsFormat) {
    prefs.update { meta -> meta.set(TransactionFormatKey, format) }
  }

  fun isChecked(id: TransactionId): Flow<Boolean> = checkedTransactionIds.map { it.getOrDefault(id, false) }

  fun isExpanded(date: LocalDate): Flow<Boolean> = combine(
    expandAllGroups,
    expandedGroups,
    transform = { expandAll, expanded -> expandAll || expanded.getOrDefault(date, true) },
  )

  fun setChecked(id: TransactionId, isChecked: Boolean) = checkedTransactionIds.update { it.put(id, isChecked) }

  fun setExpanded(date: LocalDate, isExpanded: Boolean) = expandedGroups.update { it.put(date, isExpanded) }

  fun setPrivacyMode(privacyMode: Boolean) {
    viewModelScope.launch {
      syncedPrefs[SyncedPrefKey.Global.IsPrivacyEnabled] = privacyMode.toString()
    }
  }

  fun expandAll(expand: Boolean) {
    expandAllGroups.update { expand }
    if (expand) expandedGroups.update { persistentMapOf() }
  }

  fun observe(id: TransactionId): Flow<Transaction> = transactionsDao
    .observeById(id)
    .filterNotNull()
    .distinctUntilChanged()
    .map(::toTransaction)

  private fun getIdsFlow(): Flow<List<DatedId>> = when (val spec = spec.accountSpec) {
    AccountSpec.AllAccounts ->
      transactionsDao
        .observeIds()
        .map { list -> list.map { (id, date) -> DatedId(id, date) } }

    is AccountSpec.SpecificAccount ->
      transactionsDao
        .observeIdsByAccount(spec.id)
        .map { list -> list.map { (id, date) -> DatedId(id, date) } }
  }

  private fun toDatedTransactions(datedIds: List<DatedId>, sorting: TransactionsSorting) = datedIds
    .groupBy { it.date }
    .map { (date, ids) ->
      DatedTransactions(
        date = date,
        ids = ids
          .sorted(sorting)
          .map { it.id }
          .toImmutableList(),
      )
    }.toImmutableList()

  private fun toTransaction(data: GetById): Transaction = with(data) {
    Transaction(
      id = id,
      date = date,
      account = accountName,
      payee = payeeName,
      notes = notes,
      category = categoryName,
      amount = Amount(amount),
    )
  }

  private fun List<DatedId>.sorted(sorting: TransactionsSorting) = when (sorting.direction) {
    SortDirection.Ascending -> sortedBy { it.date }
    SortDirection.Descending -> sortedByDescending { it.date }
  }

  private data class DatedId(val id: TransactionId, val date: LocalDate)

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey(Factory::class)
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(
      @Assisted token: LoginToken,
      @Assisted budgetId: BudgetId,
      @Assisted spec: TransactionsSpec,
    ): TransactionsViewModel
  }
}
