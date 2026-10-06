package aktual.budget.home.ui

import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.PrimaryTextButton
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

// Stands in for every card when the budget has nothing to show yet
@Composable
internal fun OnboardingCard(onAction: HomeActionHandler, modifier: Modifier = Modifier) {
  HomeCard(modifier = modifier) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = CardPadding, vertical = 8.dp),
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        modifier = Modifier.semantics { heading() },
        text = Strings.homeOnboardingTitle,
        style = typography.titleMedium,
        fontWeight = SemiBold,
        color = colors.pageText,
        textAlign = TextAlign.Center,
      )

      Text(
        text = Strings.homeOnboardingMessage,
        style = typography.bodyMedium,
        color = colors.pageTextSubdued,
        textAlign = TextAlign.Center,
      )

      PrimaryTextButton(
        text = Strings.homeAccountsEmptyAction,
        onClick = { onAction(OpenBankSync) },
      )
    }
  }
}

@Preview
@Composable
private fun PreviewOnboardingCard(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    OnboardingCard(modifier = Modifier.padding(16.dp), onAction = {})
  }
