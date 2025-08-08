package actual.preview

import actual.budget.list.ui.BudgetListItem
import actual.budget.model.BudgetState
import actual.core.ui.PreviewThemedColumn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun Synced() = PreviewThemedColumn {
  BudgetListItem(
    modifier = Modifier.fillMaxWidth(),
    budget = PreviewBudgetSynced,
    onClickOpen = {},
    onClickDelete = {},
  )
}

@Preview
@Composable
private fun SyncedThinner() = PreviewThemedColumn {
  BudgetListItem(
    modifier = Modifier.width(300.dp),
    budget = PreviewBudgetSynced,
    onClickOpen = {},
    onClickDelete = {},
  )
}

@Preview
@Composable
private fun Warning() = PreviewThemedColumn {
  BudgetListItem(
    modifier = Modifier.fillMaxWidth(),
    budget = PreviewBudgetSynced.copy(state = BudgetState.Broken, encryptKeyId = null),
    onClickOpen = {},
    onClickDelete = {},
  )
}
