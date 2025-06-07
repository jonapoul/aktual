package actual.budget.list.ui

import actual.budget.list.res.Strings
import actual.budget.model.Budget
import actual.core.ui.ActualFontFamily
import actual.core.ui.AlertDialog
import actual.core.ui.BareTextButton
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
internal fun DeleteBudgetDialog(
  budget: Budget,
  onAction: (DeleteDialogAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    modifier = modifier,
    title = Strings.budgetDeleteDialogTitle(budget.name),
    onDismissRequest = { onAction(DeleteDialogAction.Dismiss) },
    buttons = {
      TextButton(onClick = { onAction(DeleteDialogAction.Dismiss) }) {
        Text(
          text = Strings.budgetDeleteDialogDismiss,
          fontFamily = ActualFontFamily,
        )
      }
    },
    content = {
      Content(
        onDeleteLocal = {
          onAction(DeleteDialogAction.DeleteLocal)
          onAction(DeleteDialogAction.Dismiss)
        },
        onDeleteRemote = {
          onAction(DeleteDialogAction.DeleteRemote)
          onAction(DeleteDialogAction.Dismiss)
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
      text = annotatedString(),
      fontSize = 14.sp,
    )

    BareTextButton(
      text = Strings.budgetDeleteDialogHostedButton,
      colors = { theme, pressed -> theme.errorPrimary(pressed) },
      onClick = onDeleteRemote,
    )

    Text(
      text = Strings.budgetDeleteDialogLocalTxt,
      fontSize = 14.sp,
    )

    BareTextButton(
      text = Strings.budgetDeleteDialogLocalButton,
      colors = { theme, pressed -> theme.errorBare(pressed) },
      onClick = onDeleteLocal,
    )
  }
}

@Stable
@Composable
fun annotatedString() = buildAnnotatedString {
  append(Strings.budgetDeleteDialogHostedTxt1)
  append(" ")
  withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
    append(Strings.budgetDeleteDialogHostedTxt2)
  }
  append(" ")
  append(Strings.budgetDeleteDialogHostedTxt3)
}

@Stable
@Composable
private fun Theme.errorPrimary(isPressed: Boolean) = ButtonDefaults.buttonColors(
  containerColor = if (isPressed) buttonPrimaryBackground else errorBackground,
  contentColor = if (isPressed) buttonPrimaryText else errorText,
)

@Stable
@Composable
private fun Theme.errorBare(isPressed: Boolean) = ButtonDefaults.outlinedButtonColors(
  containerColor = if (isPressed) buttonBareBackground else buttonBareBackgroundHover,
  contentColor = if (isPressed) buttonBareText else errorText,
)

@Preview
@Composable
private fun PreviewContent() = PreviewColumn {
  Content(
    onDeleteLocal = {},
    onDeleteRemote = {},
  )
}
