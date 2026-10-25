package aktual.budget.rules.vm

import aktual.budget.model.Operator

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
    HasAnyTag,
    HasTags,
    OnBudget,
    OffBudget -> 0
  }
