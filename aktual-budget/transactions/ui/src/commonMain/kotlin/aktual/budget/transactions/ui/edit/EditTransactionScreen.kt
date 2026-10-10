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
import aktual.core.icons.Split
import aktual.core.icons.Tag
import aktual.core.icons.material.AccountBalanceWallet
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
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
import aktual.core.ui.stringLong
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        title = {},
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

// Option A of the transaction editor designs: the payee and amount up top, the rest in cards
// under them. A split lists its parts first, in place of the category
@Composable
private fun TransactionContent(
  details: TransactionDetails,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val transaction = details.transaction
  val isSplit = transaction.children.isNotEmpty()
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(horizontal = CardInset),
    verticalArrangement = Arrangement.spacedBy(CardSpacing),
  ) {
    Hero(details)

    if (isSplit) {
      SplitParts(transaction)
      DetailCard {
        AccountRow(transaction)
        CardDivider()
        NotesRow(transaction)
      }
    } else {
      DetailCard {
        DetailRow(AktualIcons.Tag, Strings.transactionCategory) { CategoryValue(transaction) }
        CardDivider()
        AccountRow(transaction)
        CardDivider()
        NotesRow(transaction)
      }

      transaction.balance?.let { BalanceAfter(it) }
    }

    BottomSpacing()
  }
}

@Composable
private fun Hero(details: TransactionDetails, modifier: Modifier = Modifier) {
  val transaction = details.transaction
  val payee = transaction.payeeText()
  Column(
    modifier = modifier.fillMaxWidth().padding(HeroPadding),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(HeroSpacing),
  ) {
    Avatar(initial = transaction.payee?.firstOrNull { it.isLetterOrDigit() })

    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(IconGap),
    ) {
      transaction.transferIcon()?.let { icon ->
        Icon(
          modifier = Modifier.size(TransferIconSize),
          imageVector = icon,
          contentDescription = null,
          tint = colors.pageText,
        )
      }
      Text(
        text = payee ?: Strings.transactionsSplitNoPayee,
        fontSize = PayeeSize,
        fontWeight = SemiBold,
        fontStyle = if (payee == null) Italic else null,
        color = if (payee == null) colors.pageTextLight else colors.pageText,
        textAlign = Center,
      )
    }

    Text(
      text = transaction.amount.formattedText(includeSign = true),
      fontSize = HeroAmountSize,
      fontWeight = Bold,
      letterSpacing = HeroAmountTracking,
      color = transaction.amount.heroColor(),
      style = tabularFigures(),
      maxLines = 1,
    )

    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(PillGap),
    ) {
      Text(
        text = "${transaction.date.dayOfWeek.stringLong()} ${transaction.date.formatted()}",
        fontSize = DateSize,
        color = colors.pageTextLight,
      )
      StatusPill(details)
    }
  }
}

@Composable
private fun Avatar(initial: Char?, modifier: Modifier = Modifier) =
  Box(
    modifier = modifier.size(AvatarSize).background(colors.mobileHeaderBackground, CircleShape),
    contentAlignment = Center,
  ) {
    if (initial != null) {
      Text(
        text = initial.uppercase(),
        fontSize = AvatarTextSize,
        fontWeight = Bold,
        color = colors.mobileHeaderText,
      )
    } else {
      Icon(
        modifier = Modifier.size(AvatarIconSize),
        imageVector = AktualIcons.Split,
        contentDescription = null,
        tint = colors.mobileHeaderText,
      )
    }
  }

@Composable
private fun StatusPill(details: TransactionDetails, modifier: Modifier = Modifier) {
  val isCleared = details.cleared || details.reconciled
  Text(
    modifier =
      modifier
        .background(
          color = if (isCleared) colors.noticeBackgroundLight else colors.pillBackground,
          shape = PillShape,
        )
        .padding(PillPadding),
    text =
      when {
        details.reconciled -> Strings.transactionReconciled
        details.cleared -> Strings.transactionCleared
        else -> Strings.transactionUncleared
      },
    fontSize = PillTextSize,
    fontWeight = SemiBold,
    color = if (isCleared) colors.noticeText else colors.pillText,
  )
}

@Composable
private inline fun DetailCard(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier.fillMaxWidth().background(colors.cardBackground, DetailCardShape),
    content = content,
  )

@Composable private fun CardDivider() = HorizontalDivider(color = colors.tableBorder)

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: @Composable () -> Unit) =
  Row(
    modifier = Modifier.fillMaxWidth().padding(RowPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconGap),
  ) {
    Icon(
      modifier = Modifier.size(RowIconSize),
      imageVector = icon,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )
    Column(verticalArrangement = Arrangement.spacedBy(LabelGap)) {
      Text(text = label, fontSize = LabelSize, color = colors.pageTextSubdued)
      value()
    }
  }

@Composable
private fun AccountRow(transaction: Transaction) =
  DetailRow(MaterialIcons.AccountBalanceWallet, Strings.transactionAccount) {
    Value(transaction.account)
  }

@Composable
private fun NotesRow(transaction: Transaction) =
  DetailRow(MaterialIcons.FormatListBulleted, Strings.transactionNotes) {
    Value(transaction.notes?.takeIf { it.isNotBlank() })
  }

// A missing value reads as "None", as upstream's mobile editor shows it
@Composable
private fun Value(text: String?, color: Color = colors.pageText) =
  Text(
    text = text ?: Strings.transactionNone,
    fontSize = ValueSize,
    color = if (text == null) colors.pageTextSubdued else color,
    fontStyle = if (text == null) Italic else null,
  )

// What the list shows in place of the category, so a transfer and an off budget transaction read
// the same here
@Composable
private fun CategoryValue(transaction: Transaction) =
  Value(text = transaction.categoryText(), color = transaction.categoryColor())

@Composable
private fun Transaction.categoryText(): String? =
  when {
    specialCategory != null -> specialCategory?.label()
    needsCategory -> Strings.transactionsNeedsCategory
    else -> category
  }

@Composable
@ReadOnlyComposable
private fun Transaction.categoryColor(): Color =
  if (specialCategory == null && needsCategory) colors.warningText else colors.pageText

@Composable
private fun BalanceAfter(balance: Amount, modifier: Modifier = Modifier) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.cardBackground, DetailCardShape)
        .padding(BalancePadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = Strings.transactionBalanceAfter,
      fontSize = DateSize,
      color = colors.pageTextLight,
    )
    Text(
      text = balance.formattedText(),
      fontSize = ValueSize,
      fontWeight = SemiBold,
      color = balance.color(),
      style = tabularFigures(),
    )
  }

@Composable
private fun SplitParts(parent: Transaction, modifier: Modifier = Modifier) =
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(SplitHeaderPadding),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(SplitHeaderGap),
    ) {
      Icon(
        modifier = Modifier.size(SplitIconSize),
        imageVector = AktualIcons.Split,
        contentDescription = null,
        tint = colors.pageTextLight,
      )
      Text(
        text = Strings.transactionSplitInto(parent.totalChildren).uppercase(),
        fontSize = SplitHeaderSize,
        fontWeight = SemiBold,
        letterSpacing = SplitHeaderTracking,
        color = colors.pageTextLight,
      )
    }

    DetailCard {
      parent.children.fastForEachIndexed { index, child ->
        if (index > 0) CardDivider()
        SplitPartRow(child, parent)
      }

      val remaining = parent.splitRemaining
      if (remaining != null) {
        CardDivider()
        Text(
          modifier = Modifier.fillMaxWidth().padding(PartPadding),
          text = Strings.transactionsSplitRemaining(remaining.formattedString()).redacted(),
          fontSize = ValueSize,
          fontWeight = SemiBold,
          color = colors.warningText,
          textAlign = End,
        )
      }
    }
  }

@Composable
private fun SplitPartRow(child: Transaction, parent: Transaction) =
  Row(
    modifier = Modifier.fillMaxWidth().padding(PartPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(PartGap),
  ) {
    Box(
      modifier =
        Modifier.size(width = PartBarWidth, height = PartBarHeight)
          .background(colors.buttonPrimaryBackground, PartBarShape),
    )

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LabelGap)) {
      CategoryValue(child)
      val subtitle = child.partSubtitle(parent)
      if (subtitle != null) {
        Text(text = subtitle, fontSize = LabelSize, color = colors.pageTextLight)
      }
    }

    Text(
      text = child.amount.formattedText(includeSign = true),
      fontSize = ValueSize,
      color = child.amount.heroColor(),
      style = tabularFigures(),
    )
  }

// A part's notes, then its payee when it isn't the one in the hero
@Composable
private fun Transaction.partSubtitle(parent: Transaction): AnnotatedString? {
  val payee = payeeLabel().takeIf { payee != parent.payee || transfer != parent.transfer }
  val notes = notes?.takeIf { it.isNotBlank() }
  val text = listOfNotNull(notes, payee).joinToString(" · ")
  return if (text.isEmpty()) null else AnnotatedString(text)
}

@Composable
private fun Transaction.payeeText(): String? =
  if (split == Parent && payee == null) null else payeeLabel()

private fun Transaction.transferIcon(): ImageVector? =
  when (transfer) {
    To -> AktualIcons.RightArrow2
    From -> AktualIcons.LeftArrow2
    null -> null
  }

// Outgoing money is red here, where the list leaves it plain
@ReadOnlyComposable
@Composable
private fun Amount.heroColor(): Color = if (this < Zero) colors.numberNegative else color()

private val CardInset = 16.dp
private val CardSpacing = 12.dp
private val DetailCardShape = RoundedCornerShape(10.dp)
private val HeroPadding = PaddingValues(start = 8.dp, top = 12.dp, end = 8.dp, bottom = 10.dp)
private val HeroSpacing = 8.dp
private val AvatarSize = 56.dp
private val AvatarIconSize = 24.dp
private val AvatarTextSize = 22.sp
private val PayeeSize = 18.sp
private val HeroAmountSize = 44.sp
private val HeroAmountTracking = (-1).sp
private val DateSize = 14.sp
private val PillGap = 8.dp
private val PillShape = RoundedCornerShape(12.dp)
private val PillPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
private val PillTextSize = 12.sp
private val TransferIconSize = 16.dp
private val IconGap = 6.dp
private val RowPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
private val RowIconSize = 20.dp
private val RowIconGap = 14.dp
private val LabelGap = 2.dp
private val LabelSize = 12.sp
private val ValueSize = 15.sp
private val BalancePadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
private val SplitHeaderPadding = PaddingValues(start = 4.dp, bottom = 8.dp)
private val SplitHeaderGap = 8.dp
private val SplitIconSize = 16.dp
private val SplitHeaderSize = 13.sp
private val SplitHeaderTracking = 1.sp
private val PartPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
private val PartGap = 12.dp
private val PartBarWidth = 4.dp
private val PartBarHeight = 32.dp
private val PartBarShape = RoundedCornerShape(2.dp)

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
