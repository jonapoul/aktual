package aktual.budget.formula.domain

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop

internal fun Assert<FormulaValue>.isError(type: FormulaErrorType, message: String? = null) =
  isInstanceOf<ErrorValue>().all {
    prop(ErrorValue::type).isEqualTo(type)
    if (message != null) prop(ErrorValue::message).isEqualTo(message)
  }
