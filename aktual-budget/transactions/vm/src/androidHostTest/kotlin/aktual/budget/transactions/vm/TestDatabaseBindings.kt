package aktual.budget.transactions.vm

import aktual.budget.db.dao.AccountDao
import aktual.budget.db.dao.CategoryDao
import aktual.budget.db.dao.PayeeDao
import aktual.budget.db.dao.TransactionDao
import aktual.di.Accessor
import aktual.di.AccessorKey
import aktual.di.BudgetScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(BudgetScope::class)
internal object TestDatabaseBindings {
  @Provides
  @IntoMap
  @AccessorKey(AccountDao::class)
  fun account(dao: AccountDao): Accessor = { dao }

  @Provides
  @IntoMap
  @AccessorKey(TransactionDao::class)
  fun transactions(dao: TransactionDao): Accessor = { dao }

  @Provides @IntoMap @AccessorKey(PayeeDao::class) fun payees(dao: PayeeDao): Accessor = { dao }

  @Provides
  @IntoMap
  @AccessorKey(CategoryDao::class)
  fun categories(dao: CategoryDao): Accessor = { dao }
}
