package aktual.budget.transactions.vm

import aktual.budget.db.dao.ClearedBalances
import aktual.budget.model.Amount

internal fun balances(
  balances: ClearedBalances?,
  options: AccountViewOptions?,
  target: Amount?,
): ClearedBalances? = balances.takeIf { options?.showExtraBalances == true || target != null }

internal fun reconciling(target: Amount?, balances: ClearedBalances?, busy: Boolean): Reconciling? =
  if (target == null || balances == null) {
    null
  } else {
    Reconciling(target, Amount(balances.cleared), busy)
  }

internal fun reconcileStart(loaded: LoadedAccount, balances: ClearedBalances?): ReconcileStart? =
  if (loaded !is SpecificAccount || balances == null) {
    null
  } else {
    ReconcileStart(
      cleared = Amount(balances.cleared),
      lastSynced = loaded.account.balance_current,
      lastReconciled = loaded.account.last_reconciled,
    )
  }
internal fun hideReconciled(options: AccountViewOptions?, target: Amount?): Boolean =
  options?.hideReconciled == true && target == null
