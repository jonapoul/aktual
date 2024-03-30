package dev.jonpoulton.actual.listbudgets.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.jonpoulton.actual.core.ui.ActualAlertDialog
import dev.jonpoulton.actual.core.ui.ActualColorScheme
import dev.jonpoulton.actual.core.ui.ActualFontFamily
import dev.jonpoulton.actual.core.ui.BareActualTextButton
import dev.jonpoulton.actual.core.ui.PreviewActualColumn
import dev.jonpoulton.actual.listbudgets.vm.Budget
import dev.jonpoulton.actual.core.res.R as ResR

@Composable
internal fun DeleteBudgetDialog(
  budget: Budget,
  onDeleteLocal: () -> Unit,
  onDeleteRemote: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  ActualAlertDialog(
    modifier = modifier,
    title = stringResource(id = ResR.string.budget_delete_dialog_title, budget.name),
    onDismissRequest = onDismiss,
    buttons = {
      TextButton(onClick = onDismiss) {
        Text(
          text = stringResource(id = ResR.string.budget_delete_dialog_dismiss),
          fontFamily = ActualFontFamily,
        )
      }
    },
    content = {
      Content(
        onDeleteLocal = {
          onDeleteLocal()
          onDismiss()
        },
        onDeleteRemote = {
          onDeleteRemote()
          onDismiss()
        },
      )
    },
  )
}

@Stable
@Composable
private fun Content(
  onDeleteLocal: () -> Unit,
  onDeleteRemote: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = buildAnnotatedString {
        append(stringResource(id = ResR.string.budget_delete_dialog_hosted_txt_1))
        append(" ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
          append(stringResource(id = ResR.string.budget_delete_dialog_hosted_txt_2))
        }
        append(" ")
        append(stringResource(id = ResR.string.budget_delete_dialog_hosted_txt_3))
      },
      fontSize = 14.sp,
    )

    BareActualTextButton(
      text = stringResource(id = ResR.string.budget_delete_dialog_hosted_button),
      colors = { scheme, pressed -> scheme.errorPrimary(pressed) },
      onClick = onDeleteRemote,
      isEnabled = false,
    )

    Text(
      text = stringResource(id = ResR.string.budget_delete_dialog_local_txt),
      fontSize = 14.sp,
    )

    BareActualTextButton(
      text = stringResource(id = ResR.string.budget_delete_dialog_local_button),
      colors = { scheme, pressed -> scheme.errorBare(pressed) },
      onClick = onDeleteLocal,
    )
  }
}

@Stable
@Composable
private fun ActualColorScheme.errorPrimary(isPressed: Boolean) = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) buttonPrimaryBackground else errorBackground,
  contentColor = if (isPressed) buttonPrimaryText else errorText,
)

@Stable
@Composable
private fun ActualColorScheme.errorBare(isPressed: Boolean) = ButtonDefaults.outlinedButtonColors(
  containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
  contentColor = if (isPressed) buttonBareText else errorText,
)

@Preview
@Composable
private fun PreviewContent() = PreviewActualColumn {
  Content(
    onDeleteLocal = {},
    onDeleteRemote = {},
  )
}
