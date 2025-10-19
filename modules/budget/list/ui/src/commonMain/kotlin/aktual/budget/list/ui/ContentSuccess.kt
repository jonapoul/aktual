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
package aktual.budget.list.ui

import aktual.budget.model.Budget
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Stable
@Composable
internal fun ContentSuccess(
  budgets: ImmutableList<Budget>,
  onClickOpen: (Budget) -> Unit,
  onClickDelete: (Budget) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    itemsIndexed(budgets) { index, budget ->
      BudgetListItem(
        budget = budget,
        theme = theme,
        onClickOpen = { onClickOpen(budget) },
        onClickDelete = { onClickDelete(budget) },
      )
    }
  }
}
