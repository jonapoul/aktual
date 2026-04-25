package aktual.budget.rules.vm

import aktual.budget.model.Operator
import aktual.budget.model.Operator.Contains
import aktual.budget.model.Operator.DoesNotContain
import aktual.budget.model.Operator.GreaterThan
import aktual.budget.model.Operator.GreaterThanOrEquals
import aktual.budget.model.Operator.HasTags
import aktual.budget.model.Operator.Is
import aktual.budget.model.Operator.IsApprox
import aktual.budget.model.Operator.IsBetween
import aktual.budget.model.Operator.IsNot
import aktual.budget.model.Operator.LessThan
import aktual.budget.model.Operator.LessThanOrEquals
import aktual.budget.model.Operator.Matches
import aktual.budget.model.Operator.NotOneOf
import aktual.budget.model.Operator.OffBudget
import aktual.budget.model.Operator.OnBudget
import aktual.budget.model.Operator.OneOf

@Suppress("MagicNumber")
internal fun Operator.score(): Int =
  when (this) {
    Is,
    IsNot -> 10
    OneOf,
    NotOneOf -> 9
    IsApprox,
    IsBetween -> 5
    GreaterThan,
    GreaterThanOrEquals,
    LessThan,
    LessThanOrEquals -> 1
    Contains,
    DoesNotContain,
    Matches,
    HasTags,
    OnBudget,
    OffBudget -> 0
  }
