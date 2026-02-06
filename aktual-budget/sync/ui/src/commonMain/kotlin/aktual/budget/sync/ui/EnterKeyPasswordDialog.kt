package aktual.budget.sync.ui

import aktual.core.l10n.Strings
import aktual.core.model.Password
import aktual.core.ui.AlertDialog
import aktual.core.ui.LocalTheme
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TextField
import aktual.core.ui.Theme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.core.ui.keyboardFocusRequester
import alakazam.compose.VerticalSpacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun EnterKeyPasswordDialog(
  input: Password,
  onAction: (SyncBudgetAction) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  AlertDialog(
    modifier = modifier,
    title = Strings.syncPasswordDialogTitle,
    onDismissRequest = { onAction(SyncBudgetAction.DismissPasswordDialog) },
    buttons = {
      TextButton(onClick = { onAction(SyncBudgetAction.DismissPasswordDialog) }) {
        Text(
          text = Strings.syncPasswordDialogDismiss,
          color = theme.buttonPrimaryText,
        )
      }

      val confirmEnabled = remember(input) { input.isNotEmpty() }
      TextButton(
        enabled = confirmEnabled,
        onClick = { onAction(SyncBudgetAction.ConfirmKeyPassword) },
      ) {
        Text(
          text = Strings.syncPasswordDialogConfirm,
          color = if (confirmEnabled) theme.buttonPrimaryText else theme.buttonNormalDisabledText,
        )
      }
    },
    content = {
      Content(
        input = input,
        theme = theme,
        onAction = onAction,
      )
    },
  )
}

@Composable
internal fun Content(
  input: Password,
  onAction: (SyncBudgetAction) -> Unit,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      modifier = Modifier.padding(horizontal = 20.dp),
      text = buildDialogText(theme, onAction),
      fontSize = 14.sp,
    )

    VerticalSpacer(4.dp)

    val keyboard = LocalSoftwareKeyboardController.current
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
      modifier = Modifier
        .padding(horizontal = 20.dp)
        .fillMaxWidth()
        .focusRequester(keyboardFocusRequester(keyboard)),
      value = input.value,
      onValueChange = { i -> onAction(SyncBudgetAction.EnterKeyPassword(Password(i))) },
      placeholderText = Strings.syncPasswordDialogPlaceholder,
      singleLine = true,
      visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      keyboardOptions = KeyboardOptions(
        autoCorrectEnabled = false,
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Go,
      ),
      keyboardActions = KeyboardActions(
        onGo = {
          keyboard?.hide()
          onAction(SyncBudgetAction.ConfirmKeyPassword)
        },
      ),
    )

    Row(
      modifier = Modifier.clickable { passwordVisible = !passwordVisible },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        modifier = Modifier.minimumInteractiveComponentSize(),
        checked = passwordVisible,
        onCheckedChange = null,
      )
      Text(text = Strings.syncPasswordDialogShowPassword)
    }
  }
}

@Stable
@Composable
private fun buildDialogText(
  theme: Theme,
  onAction: (SyncBudgetAction) -> Unit,
) = buildAnnotatedString {
  append(Strings.syncPasswordDialogText)
  append(" ")

  val style = SpanStyle(color = theme.pageTextLink, textDecoration = TextDecoration.Underline)
  val link = LinkAnnotation.Clickable(
    tag = Tags.KeyPasswordDialogLearnMore,
    linkInteractionListener = LinkInteractionListener { onAction(SyncBudgetAction.LearnMore) },
  )
  withStyle(style) {
    withLink(link) {
      append(Strings.syncPasswordDialogLearnMore)
    }
  }
}

@Preview
@Composable
private fun PreviewEnterKeyPasswordDialog(
  @PreviewParameter(PasswordProvider::class) params: ThemedParams<Password>,
) = PreviewWithColorScheme(params.type) {
  EnterKeyPasswordDialog(
    input = params.data,
    onAction = {},
  )
}

private class PasswordProvider : ThemedParameterProvider<Password>(
  Password.Empty,
  Password("abc-123"),
)
