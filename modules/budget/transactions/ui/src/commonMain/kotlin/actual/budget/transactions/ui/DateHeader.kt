package actual.budget.transactions.ui

import actual.core.ui.LocalTheme
import actual.core.ui.Theme
import actual.l10n.Strings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate

@Composable
internal fun DateHeader(
  date: LocalDate,
  source: StateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val isExpanded by source.isExpanded(date).collectAsStateWithLifecycle(initialValue = false)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(theme.tableRowHeaderBackground)
      .clickable(
        onClick = { onAction(Action.ExpandGroup(date, !isExpanded)) },
      ),
    verticalArrangement = Arrangement.Bottom,
  ) {
    Box(
      contentAlignment = Alignment.CenterVertically + Alignment.End,
    ) {
      Text(
        modifier = Modifier.fillMaxSize(),
        text = date.toString(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = theme.tableHeaderText,
        fontSize = 14.sp,
      )

      Icon(
        modifier = Modifier.minimumInteractiveComponentSize(),
        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
        contentDescription = contentDescription(isExpanded),
        tint = theme.tableHeaderText,
      )
    }

    HorizontalDivider(color = theme.tableBorderSeparator)
  }
}

@Composable
private fun contentDescription(isExpanded: Boolean): String = when (isExpanded) {
  true -> Strings.transactionsCollapse
  false -> Strings.transactionsExpand
}
