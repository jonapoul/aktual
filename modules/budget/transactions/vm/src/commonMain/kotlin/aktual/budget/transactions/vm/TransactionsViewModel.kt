/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.transactions.vm

import aktual.budget.db.dao.AccountsDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.TransactionsDao
import aktual.budget.db.transactions.GetById
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.BudgetId
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsFormat
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount.AllAccounts
import aktual.budget.transactions.vm.LoadedAccount.Loading
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.core.di.AssistedFactoryKey
import aktual.core.di.BudgetGraphHolder
import aktual.core.di.ViewModelAssistedFactory
import aktual.core.di.ViewModelScope
import aktual.core.di.throwIfWrongBudget
import aktual.core.model.LoginToken
import alakazam.kotlin.core.CoroutineContexts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
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
    .map { meta -> meta[TransactionFormatDelegate] }
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsFormat.Default)

  val transactions: StateFlow<ImmutableList<DatedTransactions>> = getIdsFlow()
    .distinctUntilChanged()
    .map(::toDatedTransactions)
    .stateIn(viewModelScope, Eagerly, initialValue = persistentListOf())

  val sorting: StateFlow<TransactionsSorting> = prefs
    .map(::TransactionsSorting)
    .stateIn(viewModelScope, Eagerly, initialValue = TransactionsSorting.Default)

  init {
    budgetGraph.throwIfWrongBudget(budgetId)

    when (val spec = spec.accountSpec) {
      is AccountSpec.AllAccounts -> mutableLoadedAccount.update { AllAccounts }
      is AccountSpec.SpecificAccount -> viewModelScope.launch {
        val account = accountsDao[spec.id] ?: error("No account matching $spec")
        mutableLoadedAccount.update { SpecificAccount(account) }
      }
    }
  }

  fun setFormat(format: TransactionsFormat) {
    prefs.update { meta -> meta.set(TransactionFormatDelegate, format) }
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

  private fun toDatedTransactions(datedIds: List<DatedId>) = datedIds
    .groupBy { it.date }
    .map { (date, datedIds) -> DatedTransactions(date, datedIds.map { it.id }.toImmutableList()) }
    .toImmutableList()

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

  private data class DatedId(val id: TransactionId, val date: LocalDate)

  @AssistedFactory
  @AssistedFactoryKey(Factory::class)
  @ContributesIntoMap(ViewModelScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    fun create(
      @Assisted token: LoginToken,
      @Assisted budgetId: BudgetId,
      @Assisted spec: TransactionsSpec,
    ): TransactionsViewModel
  }
}
