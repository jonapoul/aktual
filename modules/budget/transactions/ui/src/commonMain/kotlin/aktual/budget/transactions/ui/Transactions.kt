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
package aktual.budget.transactions.ui

import aktual.budget.model.TransactionsFormat
import aktual.budget.transactions.vm.DatedTransactions
import aktual.budget.transactions.vm.TransactionsSorting
import aktual.core.ui.Dimens
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.core.ui.scrollbar
import aktual.l10n.Strings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun Transactions(
  transactions: ImmutableList<DatedTransactions>,
  observer: TransactionObserver,
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  source: StateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  if (transactions.isEmpty()) {
    TransactionsEmpty(
      sorting = sorting,
      modifier = modifier,
      theme = theme,
    )
  } else {
    TransactionsFilled(
      transactions = transactions,
      observer = observer,
      format = format,
      sorting = sorting,
      source = source,
      onAction = onAction,
      modifier = modifier,
      theme = theme,
    )
  }
}

@Composable
private fun TransactionsEmpty(
  sorting: TransactionsSorting,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxHeight(),
  ) {
    CategoryHeader(
      modifier = Modifier.fillMaxWidth(),
      sorting = sorting,
      onSort = {},
      theme = theme,
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = Strings.transactionsEmpty,
        textAlign = TextAlign.Center,
        fontStyle = FontStyle.Italic,
        color = theme.tableText,
      )
    }
  }
}

@Composable
private fun TransactionsFilled(
  transactions: ImmutableList<DatedTransactions>,
  observer: TransactionObserver,
  format: TransactionsFormat,
  sorting: TransactionsSorting,
  source: StateSource,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val listState = rememberLazyListState()
  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = Dimens.Large)
      .scrollbar(listState),
    state = listState,
  ) {
    if (format == TransactionsFormat.Table) {
      stickyHeader {
        CategoryHeader(
          modifier = Modifier.fillMaxWidth(),
          sorting = sorting,
          onSort = {},
          theme = theme,
        )
      }
    }

    transactions.fastForEach { (date, ids) ->
      stickyHeader {
        DateHeader(
          modifier = Modifier.fillMaxWidth(),
          date = date,
          source = source,
          onAction = onAction,
          theme = theme,
        )
      }

      items(ids) { id ->
        TransactionItem(
          modifier = Modifier.fillMaxWidth(),
          id = id,
          observer = observer,
          format = format,
          source = source,
          onAction = onAction,
          theme = theme,
        )

        HorizontalDivider(color = theme.tableBorderSeparator)
      }
    }
  }
}
