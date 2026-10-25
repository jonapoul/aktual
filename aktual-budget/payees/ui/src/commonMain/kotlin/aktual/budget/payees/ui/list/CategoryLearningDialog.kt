package aktual.budget.payees.ui.list

import aktual.core.icons.AktualIcons
import aktual.core.icons.LightBulb
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColoredBooleanParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

// CategoryLearning.tsx
@Composable
internal fun CategoryLearningDialog(
  isEnabled: Boolean,
  onEnabledChange: (Boolean) -> Unit,
  onDismiss: () -> Unit,
) {
  AktualAlertDialog(
    title = Strings.payeesCategoryLearning,
    icon = AktualIcons.LightBulb,
    highlight = colors.noticeText,
    onDismissRequest = onDismiss,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.payeesCategoryLearningClose) }
      TextButton(onClick = { onEnabledChange(!isEnabled) }) {
        Text(
          color = colors.noticeText,
          text =
            if (isEnabled) {
              Strings.payeesCategoryLearningDisable
            } else {
              Strings.payeesCategoryLearningEnable
            },
        )
      }
    },
  ) {
    Text(text = learnMoreText())
    Text(text = Strings.payeesCategoryLearningDisableMessage)
  }
}

@Composable
private fun learnMoreText(): AnnotatedString {
  val message = Strings.payeesCategoryLearningMessage
  val learnMore = Strings.payeesCategoryLearningLearnMore
  val linkStyle = SpanStyle(color = colors.pageTextLink, textDecoration = Underline)
  return remember(message, learnMore, linkStyle) {
    buildAnnotatedString {
      append(message)
      append(" ")
      withLink(LinkAnnotation.Url(url = LEARN_MORE_URL, styles = TextLinkStyles(linkStyle))) {
        append(learnMore)
      }
    }
  }
}

private const val LEARN_MORE_URL = "https://actualbudget.org/docs/budgeting/rules/#automatic-rules"

@Preview
@Composable
private fun PreviewCategoryLearningDialog(
  @PreviewParameter(ColoredBooleanParameters::class) params: ColoredParams<Boolean>,
) =
  PreviewWithColoredParams(params) {
    CategoryLearningDialog(isEnabled = this, onEnabledChange = {}, onDismiss = {})
  }
