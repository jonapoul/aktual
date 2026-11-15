package aktual.budget.schedules.domain

import aktual.budget.model.AmountOperator
import aktual.budget.model.Operator

fun AmountOperator.amountPrefix(): String =
  when (this) {
    Operator.IsApprox,
    Operator.IsBetween -> "~"

    Operator.GreaterThan,
    Operator.GreaterThanOrEquals,
    Operator.Is,
    Operator.LessThan,
    Operator.LessThanOrEquals -> ""
  }
