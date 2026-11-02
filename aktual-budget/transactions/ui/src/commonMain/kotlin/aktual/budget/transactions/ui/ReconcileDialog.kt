package aktual.budget.transactions.ui

import aktual.budget.model.Amount
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toSignedInputText
import aktual.budget.transactions.vm.ReconcileStart
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.AmountField
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.disabled
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// The bank balance to reconcile with, as upstream's AccountReconcileModal. It starts at the cleared
// balance
@Composable
internal fun ReconcileDialog(
  start: ReconcileStart,
  onConfirm: (Amount) -> Unit,
  onDismiss: () -> Unit,
) =
  AktualAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnClickOutside = false),
  ) {
    ReconcileDialogContent(
      start = start,
      state = rememberTextFieldState(start.cleared.toSignedInputText()),
      onConfirm = onConfirm,
      onDismiss = onDismiss,
    )
  }

@Composable
private fun ReconcileDialogContent(
  start: ReconcileStart,
  state: TextFieldState,
  onConfirm: (Amount) -> Unit,
  onDismiss: () -> Unit,
) {
  val amount = evaluateAmountInput(state.text.toString())
  val isValid = amount != null
  val confirm: () -> Unit = { amount?.let(onConfirm) }
  AktualAlertDialogContent(
    title = Strings.transactionsReconcile,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.transactionsReconcileCancel) }
      TextButton(enabled = isValid, onClick = confirm) {
        Text(
          text = Strings.transactionsReconcileConfirm,
          fontWeight = if (isValid) Bold else null,
          color = if (isValid) colors.numberPositive else colors.buttonNormalDisabledText.disabled,
        )
      }
    },
    content = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = Strings.transactionsReconcileMessage, style = typography.bodyMedium)

        AmountField(
          modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
          state = state,
          arithmetic = true,
          onKeyboardAction = { confirm() },
        )

        val lastSynced = start.lastSynced
        if (lastSynced != null) {
          Text(
            text = Strings.transactionsReconcileLastSynced(lastSynced.formattedString()),
            style = typography.bodySmall,
            color = colors.pageTextLight,
          )
          NormalTextButton(
            modifier = Modifier.fillMaxWidth(),
            text = Strings.transactionsReconcileUseLastSynced,
            onClick = { state.setTextAndPlaceCursorAtEnd(lastSynced.toSignedInputText()) },
          )
        }

        Text(
          text = lastReconciledText(start.lastReconciled),
          style = typography.bodySmall,
          color = colors.pageTextLight,
        )
      }
    },
  )
}

@Composable
private fun lastReconciledText(lastReconciled: Instant?): String {
  if (lastReconciled == null) return Strings.transactionsReconcileNever
  val date = lastReconciled.toLocalDateTime(TimeZone.currentSystemDefault()).date
  return Strings.transactionsReconcileLastReconciled(date.formatted())
}

@Preview
@Composable
private fun PreviewReconcileDialog(
  @PreviewParameter(ReconcileDialogProvider::class) params: ColorParams<ReconcileStart>,
) =
  PreviewWithColorParams(params) {
    ReconcileDialogContent(
      start = this,
      state = rememberTextFieldState(cleared.toSignedInputText()),
      onConfirm = {},
      onDismiss = {},
    )
  }

private class ReconcileDialogProvider :
  ColorParamsProvider<ReconcileStart>(
    ReconcileStart(cleared = PREVIEW_BALANCE, lastSynced = null, lastReconciled = null),
    ReconcileStart(
      cleared = PREVIEW_DEBT,
      lastSynced = PREVIEW_SYNCED,
      lastReconciled = PREVIEW_RECONCILED,
    ),
  )

private val PREVIEW_DEBT = Amount(-123.45)
private val PREVIEW_SYNCED = Amount(-100.00)
private val PREVIEW_RECONCILED = Instant.parse("2026-09-21T10:00:00Z")
