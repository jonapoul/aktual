package aktual.budget.transactions.ui

import aktual.budget.model.Amount
import aktual.budget.transactions.vm.Reconciling
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheckCircle1
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.NormalTextButton
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.formattedString
import aktual.core.ui.redacted
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

// Upstream's ReconcilingBanner: locks the cleared transactions once they match the bank's balance,
// or offers an adjustment for the difference
@Composable
internal fun ReconcilingBanner(
  reconciling: Reconciling,
  onAction: OnAction<Action>,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.fillMaxWidth().background(colors.tableBackground)) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalAlignment = CenterHorizontally,
    ) {
      if (reconciling.difference == Zero) {
        Row(
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            modifier = Modifier.size(14.dp),
            imageVector = AktualIcons.CheckCircle1,
            contentDescription = null,
            tint = colors.noticeTextLight,
          )
          Text(text = Strings.transactionsReconcileBalanced, color = colors.noticeTextLight)
        }

        PrimaryTextButton(
          modifier = Modifier.fillMaxWidth(),
          text = Strings.transactionsReconcileLock,
          isEnabled = !reconciling.isBusy,
          onClick = { onAction(FinishReconciling) },
        )
      } else {
        Text(
          text = differenceText(reconciling),
          color = colors.tableText,
          fontStyle = Italic,
          textAlign = Center,
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = CenterVertically,
        ) {
          NormalTextButton(
            modifier = Modifier.weight(1f),
            text = Strings.transactionsReconcileCreateAdjustment,
            isEnabled = !reconciling.isBusy,
            onClick = { onAction(CreateReconcileAdjustment) },
          )
          PrimaryTextButton(
            modifier = Modifier.weight(1f),
            text = Strings.transactionsReconcileExit,
            isEnabled = !reconciling.isBusy,
            onClick = { onAction(FinishReconciling) },
          )
        }
      }
    }

    HorizontalDivider()
  }

// The amounts in bold, as upstream's
@Composable
private fun differenceText(reconciling: Reconciling): AnnotatedString {
  val cleared = reconciling.cleared.formattedString()
  val difference = reconciling.difference.formattedString(includeSign = true)
  val target = reconciling.target.formattedString()
  val text = Strings.transactionsReconcileDifference(cleared, difference, target)
  return buildAnnotatedString {
    append(text)
    var from = 0
    for (value in listOf(cleared, difference, target)) {
      val start = text.indexOf(value, from)
      if (start < 0) continue
      addStyle(SpanStyle(fontWeight = Bold), start, start + value.length)
      from = start + value.length
    }
  }
    .redacted()
}

@Preview
@Composable
private fun PreviewReconcilingBanner(
  @PreviewParameter(ReconcilingBannerProvider::class) params: ColorParams<Reconciling>,
) = PreviewWithColorParams(params) { ReconcilingBanner(reconciling = this, onAction = {}) }

private class ReconcilingBannerProvider :
  ColorParamsProvider<Reconciling>(
    Reconciling(target = PREVIEW_BALANCE, cleared = PREVIEW_BALANCE),
    Reconciling(target = PREVIEW_ABOVE, cleared = PREVIEW_BALANCE),
    Reconciling(target = PREVIEW_BELOW, cleared = PREVIEW_BALANCE, isBusy = true),
  )

private val PREVIEW_ABOVE = Amount(3500.00)
private val PREVIEW_BELOW = Amount(3400.00)
