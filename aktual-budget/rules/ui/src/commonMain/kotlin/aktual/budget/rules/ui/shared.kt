package aktual.budget.rules.ui

import aktual.budget.model.ConditionOptions
import aktual.budget.model.Field
import aktual.budget.model.Operator
import aktual.budget.rules.vm.label
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Field.string(options: ConditionOptions?): String = stringResource(label(options))

@Composable internal fun Operator.displayString(): String = stringResource(label())
