package dev.jonpoulton.actual.listbudgets.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.listbudgets.vm.Budget
import dev.jonpoulton.actual.core.res.R as ResR

@Suppress("UNUSED_PARAMETER")
@Composable
internal fun DeleteBudgetDialog(
  budget: Budget,
  onDeleteLocal: () -> Unit,
  onDeleteRemote: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  // TODO: IMPLEMENT
  DialogTitle()
}

@Stable
@Composable
private fun DialogTitle() {
  Text(
    text = stringResource(id = ResR.string.budget_delete_dialog_title),
    fontFamily = ActualFontFamily,
    fontWeight = FontWeight.W700,
    fontSize = 25.sp,
  )
}
