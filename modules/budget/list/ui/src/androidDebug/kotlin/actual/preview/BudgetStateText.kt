package actual.preview

import actual.budget.list.ui.BudgetStateText
import actual.budget.model.BudgetState
import actual.core.ui.PreviewThemedRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewStates() = PreviewThemedRow {
  LazyColumn {
    items(BudgetState.entries) {
      BudgetStateText(it)
    }
  }
}
