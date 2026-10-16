package aktual.budget.formula.domain

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop

internal fun Assert<FormulaValue>.isError(
  type: FormulaErrorType,
  reason: ErrorReason? = null,
  subject: String? = null,
) =
  isInstanceOf<ErrorValue>().all {
    prop(ErrorValue::type).isEqualTo(type)
    if (reason != null) prop(ErrorValue::reason).isEqualTo(reason)
    if (subject != null) prop(ErrorValue::subject).isEqualTo(subject)
  }
