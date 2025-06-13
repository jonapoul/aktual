package actual.budget.transactions.ui

import actual.core.ui.PreviewColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate

@Composable
internal fun DateHeader(
  date: LocalDate,
  header: TransactionHeader,
  modifier: Modifier = Modifier,
) {
  val isExpanded by header.isExpanded(date).collectAsStateWithLifecycle(initialValue = false)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable(
        indication = ripple(),
        interactionSource = remember { MutableInteractionSource() },
        onClick = { header.onExpandedChange(date, !isExpanded) },
      ),
  ) {
    Text(
      text = date.toString(),
    )
  }
}

@Preview
@Composable
private fun PreviewDateHeader() = PreviewColumn {
  DateHeader(
    date = PREVIEW_DATE,
    header = PreviewTransactionHeader,
  )
}
