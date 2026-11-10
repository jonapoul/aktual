package aktual.budget.accounts.components

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// A sheet page with a title, its content, then a row of buttons along the bottom
@Composable
fun SheetPage(
  title: String,
  modifier: Modifier = Modifier,
  buttons: (@Composable RowScope.() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScrollWithBar()
        .padding(horizontal = SheetPageDS.padding)
        .padding(bottom = SheetPageDS.padding),
    verticalArrangement = Arrangement.spacedBy(SheetPageDS.spacing),
  ) {
    Text(text = title, style = typography.headlineSmall)
    content()
    if (buttons != null) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SheetPageDS.buttonSpacing),
        content = buttons,
      )
    }
  }
}

@Composable
fun SheetLoading(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth().height(160.dp), contentAlignment = Center) {
    CircularProgressIndicator(color = colors.pageTextSubdued)
  }
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
  Text(
    modifier = modifier.padding(bottom = 4.dp),
    text = text,
    style = typography.labelLarge,
    color = colors.pageTextSubdued,
  )
}

@Composable
fun ErrorText(text: String, modifier: Modifier = Modifier) {
  Text(modifier = modifier, text = text, style = typography.bodyMedium, color = colors.errorText)
}

private object SheetPageDS {
  val padding = 20.dp
  val spacing = 16.dp
  val buttonSpacing = 12.dp
}
