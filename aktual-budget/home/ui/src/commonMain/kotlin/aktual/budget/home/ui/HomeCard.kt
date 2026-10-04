package aktual.budget.home.ui

import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.RounderCardShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp.Companion.Hairline
import androidx.compose.ui.unit.dp

internal val CardPadding = 16.dp

@Composable
internal fun HomeCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RounderCardShape)
        .background(colors.tableBackground, RounderCardShape)
        .border(Hairline, colors.tableBorder, RounderCardShape)
        .padding(vertical = CardPadding),
    content = content,
  )
}

internal fun TextStyle.tabularFigures() = copy(fontFeatureSettings = "tnum")
