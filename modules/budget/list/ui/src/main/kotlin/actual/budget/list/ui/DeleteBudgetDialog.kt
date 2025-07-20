package actual.budget.list.ui

import actual.budget.list.vm.DeletingState
import actual.budget.model.Budget
import actual.core.ui.ActualFontFamily
import actual.core.ui.ActualTypography
import actual.core.ui.AlertDialog
import actual.core.ui.BareTextButton
import actual.core.ui.LocalTheme
import actual.core.ui.PreviewColumn
import actual.core.ui.Theme
import actual.core.ui.buttonBare
import actual.l10n.Strings
import alakazam.android.ui.compose.HorizontalSpacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DeleteBudgetDialog(
  budget: Budget,
  deletingState: DeletingState,
  localFileExists: Boolean,
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
        deletingState = deletingState,
        localFileExists = localFileExists,
        onDeleteLocal = { onAction(DeleteDialogAction.DeleteLocal) },
        onDeleteRemote = { onAction(DeleteDialogAction.DeleteRemote) },
      )
    },
  )
}

@Stable
@Composable
private fun Content(
  deletingState: DeletingState,
  localFileExists: Boolean,
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

    var hasPressedDeleteButton by remember { mutableStateOf(false) }
    val isNotDeleting = deletingState is DeletingState.Inactive

    var firstCheckbox by remember { mutableStateOf(false) }
    var secondCheckbox by remember { mutableStateOf(false) }

    Row {
      Switch(
        checked = firstCheckbox,
        onCheckedChange = { firstCheckbox = !firstCheckbox },
      )

      HorizontalSpacer(20.dp)

      Switch(
        checked = secondCheckbox,
        onCheckedChange = { secondCheckbox = !secondCheckbox },
      )
    }

    LoadableBareTextButton(
      text = Strings.budgetDeleteDialogHostedButton,
      colors = { theme, pressed -> theme.errorPrimary(pressed) },
      isEnabled = isNotDeleting && firstCheckbox && secondCheckbox,
      isLoading = deletingState is DeletingState.Active && deletingState.deletingRemote,
      onClick = {
        hasPressedDeleteButton = true
        onDeleteRemote()
      },
    )

    if (localFileExists) {
      Text(
        text = Strings.budgetDeleteDialogLocalTxt,
        fontSize = 14.sp,
      )

      LoadableBareTextButton(
        text = Strings.budgetDeleteDialogLocalButton,
        colors = { theme, pressed -> theme.errorBare(pressed) },
        isEnabled = isNotDeleting,
        isLoading = deletingState is DeletingState.Active && deletingState.deletingLocal,
        onClick = {
          hasPressedDeleteButton = true
          onDeleteLocal()
        },
      )
    }
  }
}

private val IconPadding = 10.dp
private val IconSize = 32.dp
private val ProgressWheelStrokeWidth = 5.dp

@Composable
private fun LoadableBareTextButton(
  text: String,
  onClick: () -> Unit,
  isEnabled: Boolean,
  isLoading: Boolean,
  colors: @Composable (Theme, Boolean) -> ButtonColors,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) = Box(
  modifier = modifier,
  contentAlignment = Alignment.Center,
) {
  BareTextButton(
    text = text,
    colors = colors,
    isEnabled = isEnabled,
    onClick = onClick,
    style = if (isLoading) TextStyle(color = Transparent) else ActualTypography.buttonBare,
  )

  Box(
    modifier = modifier.padding(IconPadding),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator(
      modifier = Modifier
        .size(IconSize)
        .alpha(if (isLoading) 1f else 0f),
      color = theme.sliderActiveTrack,
      trackColor = theme.sliderInactiveTrack,
      strokeWidth = ProgressWheelStrokeWidth,
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
    deletingState = DeletingState.Inactive,
    localFileExists = true,
    onDeleteLocal = {},
    onDeleteRemote = {},
  )
}

@Preview
@Composable
private fun PreviewDeletingLocal() = PreviewColumn {
  Content(
    deletingState = DeletingState.Active(deletingLocal = true),
    localFileExists = true,
    onDeleteLocal = {},
    onDeleteRemote = {},
  )
}

@Preview
@Composable
private fun PreviewDeletingRemote() = PreviewColumn {
  Content(
    deletingState = DeletingState.Active(deletingRemote = true),
    localFileExists = true,
    onDeleteLocal = {},
    onDeleteRemote = {},
  )
}

@Preview
@Composable
private fun PreviewNoLocal() = PreviewColumn {
  Content(
    deletingState = DeletingState.Active(deletingRemote = true),
    localFileExists = false,
    onDeleteLocal = {},
    onDeleteRemote = {},
  )
}
