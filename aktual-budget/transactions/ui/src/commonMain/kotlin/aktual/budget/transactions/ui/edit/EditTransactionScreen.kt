package aktual.budget.transactions.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.transactions.ui.TRANSACTION_1
import aktual.budget.transactions.ui.TRANSACTION_OFF_BUDGET
import aktual.budget.transactions.ui.TRANSACTION_SPLIT_UNBALANCED
import aktual.budget.transactions.ui.TRANSACTION_TRANSFER
import aktual.budget.transactions.ui.color
import aktual.budget.transactions.ui.label
import aktual.budget.transactions.ui.payeeLabel
import aktual.budget.transactions.ui.tabularFigures
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.edit.EditTransactionState
import aktual.budget.transactions.vm.edit.EditTransactionViewModel
import aktual.budget.transactions.vm.edit.TransactionDetails
import aktual.core.icons.AktualIcons
import aktual.core.icons.LeftArrow2
import aktual.core.icons.RightArrow2
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LoadingScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.formattedText
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.redacted
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
internal fun EditTransactionScreen(
  id: TransactionId,
  back: BackNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditTransactionViewModel = editTransactionViewModel(id),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  EditTransactionScaffold(modifier = modifier, state = state, onBack = { back() })
}

@Composable
private fun editTransactionViewModel(id: TransactionId) =
  assistedMetroViewModel<EditTransactionViewModel, EditTransactionViewModel.Factory>(
    key = id.toString(),
  ) {
    create(id)
  }

@Composable
private fun EditTransactionScaffold(
  state: EditTransactionState,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton(onClick = onBack) },
        title = { Text(text = Strings.transactionTitle) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading -> LoadingScreen(modifier = Modifier.padding(innerPadding))

        is Failure ->
          FailureScreen(
            modifier = Modifier.padding(innerPadding),
            title = Strings.transactionFailureTitle,
            reason =
              when (state) {
                NotFound -> Strings.transactionFailureNotFound
                is Other -> state.reason
              },
            action =
              FailureAction(
                text = { Strings.navBack },
                icon = MaterialIcons.ArrowBack,
                onClick = onBack,
              ),
          )

        is Loaded ->
          TransactionContent(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            details = state.saved,
            scrollState = scrollState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
          )
      }
    }
  }
}

@Composable
private fun TransactionContent(
  details: TransactionDetails,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val transaction = details.transaction
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(ContentPadding),
    verticalArrangement = Arrangement.spacedBy(SectionSpacing),
  ) {
    Header(transaction)

    FieldCard {
      Field(label = Strings.transactionPayee) { PayeeValue(transaction) }
      CardDivider()
      Field(label = Strings.transactionCategory) { CategoryValue(transaction) }
      CardDivider()
      Field(label = Strings.transactionAccount) { Value(transaction.account) }
      CardDivider()
      Field(label = Strings.transactionDate) { Value(transaction.date.formatted()) }
      CardDivider()
      Field(label = Strings.transactionStatus) { Value(details.statusLabel()) }
      CardDivider()
      Field(label = Strings.transactionNotes) {
        Value(transaction.notes?.takeIf { it.isNotBlank() })
      }
    }

    if (transaction.children.isNotEmpty()) SplitParts(transaction)

    BottomSpacing()
  }
}

@Composable
private fun Header(transaction: Transaction, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier.fillMaxWidth(),
    text = transaction.amount.formattedText(includeSign = true),
    style = tabularFigures(typography.headlineMedium),
    color = transaction.amount.color(),
    textAlign = Center,
  )

@Composable
private fun FieldCard(content: @Composable () -> Unit) =
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(colors.tableBackground, CardShape)
        .border(Hairline, colors.tableBorder, CardShape),
  ) {
    content()
  }

@Composable private fun CardDivider() = HorizontalDivider(color = colors.tableBorder)

@Composable
private fun Field(label: String, value: @Composable () -> Unit) =
  Row(
    modifier = Modifier.fillMaxWidth().padding(RowPadding),
    horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
  ) {
    Text(
      modifier = Modifier.weight(LABEL_WEIGHT),
      text = label,
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )
    Box(modifier = Modifier.weight(VALUE_WEIGHT)) { value() }
  }

// A missing value reads as "None", as upstream's mobile editor shows it
@Composable
private fun Value(text: String?, color: Color = colors.pageText) =
  Value(text = text?.let { AnnotatedString(it) }, color = color)

@Composable
private fun Value(text: AnnotatedString?, color: Color = colors.pageText) =
  Text(
    text = text ?: AnnotatedString(Strings.transactionNone),
    style = typography.bodyMedium,
    color = if (text == null) colors.pageTextSubdued else color,
    fontStyle = if (text == null) Italic else null,
  )

@Composable
private fun PayeeValue(transaction: Transaction) {
  val noPayee = transaction.split == Parent && transaction.payee == null
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(IconGap),
  ) {
    val transferIcon =
      when (transaction.transfer) {
        To -> AktualIcons.RightArrow2
        From -> AktualIcons.LeftArrow2
        null -> null
      }
    if (transferIcon != null) {
      Icon(
        modifier = Modifier.size(IconSize),
        imageVector = transferIcon,
        contentDescription = null,
        tint = colors.pageText,
      )
    }
    Value(if (noPayee) Strings.transactionsSplitNoPayee else transaction.payeeLabel())
  }
}

// What the list shows in place of the category, so a split, a transfer and an off budget
// transaction read the same here
@Composable
private fun CategoryValue(transaction: Transaction) {
  val special = transaction.specialCategory?.label()
  when {
    transaction.split == Parent -> Value(Strings.transactionsSplit)
    special != null -> Value(special)
    transaction.needsCategory -> Value(Strings.transactionsNeedsCategory, colors.warningText)
    else -> Value(transaction.category)
  }
}

@Composable
private fun TransactionDetails.statusLabel(): String =
  when {
    reconciled -> Strings.transactionReconciled
    cleared -> Strings.transactionCleared
    else -> Strings.transactionUncleared
  }

@Composable
private fun SplitParts(parent: Transaction, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(LabelSpacing),
  ) {
    Text(text = Strings.transactionSplitParts, style = typography.labelLarge)

    FieldCard {
      parent.children.fastForEachIndexed { index, child ->
        if (index > 0) CardDivider()
        SplitPartRow(child)
      }

      val remaining = parent.splitRemaining
      if (remaining != null) {
        CardDivider()
        Text(
          modifier = Modifier.fillMaxWidth().padding(RowPadding),
          text = Strings.transactionsSplitRemaining(remaining.formattedString()).redacted(),
          style = typography.bodyMedium,
          fontWeight = SemiBold,
          color = colors.warningText,
          textAlign = End,
        )
      }
    }
  }
}

@Composable
private fun SplitPartRow(child: Transaction) =
  Row(
    modifier = Modifier.fillMaxWidth().padding(RowPadding),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(FieldSpacing),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      PayeeValue(child)
      CategoryValue(child)
      child.notes
        ?.takeIf { it.isNotBlank() }
        ?.let { notes ->
          Text(text = notes, style = typography.bodySmall, color = colors.pageTextSubdued)
        }
    }

    AmountText(child.amount)
  }

@Composable
private fun AmountText(amount: Amount) =
  Text(
    text = amount.formattedText(includeSign = true),
    style = tabularFigures(typography.bodyMedium),
    color = amount.color(),
    textAlign = End,
  )

private val ContentPadding = 16.dp
private val SectionSpacing = 20.dp
private val FieldSpacing = 12.dp
private val LabelSpacing = 6.dp
private val RowPadding = 12.dp
private val Hairline = 1.dp
private val IconSize = 14.dp
private val IconGap = 5.dp
private const val LABEL_WEIGHT = 1f
private const val VALUE_WEIGHT = 2f

@PortraitPreview
@Composable
private fun PreviewEditTransactionScaffold(
  @PreviewParameter(EditTransactionStateProvider::class)
  params: ColoredParams<EditTransactionState>,
) = PreviewWithColoredParams(params) { EditTransactionScaffold(state = this, onBack = {}) }

private fun previewLoaded(
  transaction: Transaction,
  cleared: Boolean = true,
  reconciled: Boolean = false,
) =
  EditTransactionState.Loaded(
    saved = TransactionDetails(transaction, cleared = cleared, reconciled = reconciled),
    mode = View,
    canEdit = true,
  )

private class EditTransactionStateProvider :
  ColoredParameterProvider<EditTransactionState>(
    Loading,
    EditTransactionState.Failure.NotFound,
    previewLoaded(TRANSACTION_1),
    previewLoaded(TRANSACTION_TRANSFER.copy(notes = null), reconciled = true),
    previewLoaded(TRANSACTION_OFF_BUDGET, cleared = false),
    previewLoaded(TRANSACTION_SPLIT_UNBALANCED),
  )
