package aktual.budget.rules.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.CategoryGroupDao
import aktual.budget.db.dao.PayeeDao
import aktual.di.BudgetScope
import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.ContributesBinding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable data class EntitySummary(val id: String, val name: String)

@Immutable
interface EntityListFetcher {
  suspend fun payees(): ImmutableList<EntitySummary>

  suspend fun accounts(): ImmutableList<EntitySummary>

  suspend fun categories(): ImmutableList<EntitySummary>

  suspend fun categoryGroups(): ImmutableList<EntitySummary>
}

@ContributesBinding(BudgetScope::class)
class EntityListFetcherImpl(database: BudgetDatabase) : EntityListFetcher {
  private val payees = PayeeDao(database)
  private val accounts = AccountDao(database)
  private val categories = CategoryDao(database)
  private val categoryGroups = CategoryGroupDao(database)

  override suspend fun payees(): ImmutableList<EntitySummary> =
    payees
      .getAllActive()
      .mapNotNull { row -> row.name?.let { EntitySummary(row.id.toString(), it) } }
      .toImmutableList()

  override suspend fun accounts(): ImmutableList<EntitySummary> =
    accounts
      .getAllActive()
      .mapNotNull { row -> row.name?.let { EntitySummary(row.id.toString(), it) } }
      .toImmutableList()

  override suspend fun categories(): ImmutableList<EntitySummary> =
    categories
      .getAllActive()
      .mapNotNull { row -> row.name?.let { EntitySummary(row.id.toString(), it) } }
      .toImmutableList()

  override suspend fun categoryGroups(): ImmutableList<EntitySummary> =
    categoryGroups
      .getAllActive()
      .mapNotNull { row -> row.name?.let { EntitySummary(row.id.toString(), it) } }
      .toImmutableList()
}
