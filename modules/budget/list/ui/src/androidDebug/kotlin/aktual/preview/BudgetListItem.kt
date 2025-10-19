/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.preview

import aktual.budget.list.ui.BudgetListItem
import aktual.budget.model.BudgetState
import aktual.core.ui.PreviewThemedColumn
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
