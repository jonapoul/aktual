package aktual.budget.banksync.ui

import aktual.budget.banksync.vm.BankSyncAccount
import aktual.budget.banksync.vm.BankSyncAccountStatus
import aktual.budget.banksync.vm.BankSyncProviderStatus
import aktual.budget.banksync.vm.LastBankSync
import aktual.budget.model.AccountSyncSource
import aktual.core.icons.material.AccountBalance
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** Keep in sync with [ShimmerBankSyncAccountItem] */
@Composable
internal fun BankSyncAccountItem(
  account: BankSyncAccount,
  isLinked: Boolean,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .padding(BankSyncDS.itemCardPadding),
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(BankSyncDS.itemContentSpacing),
    ) {
      Text(
        text = account.name ?: Strings.bankSyncUnnamedAccount,
        style = typography.bodyMedium,
        fontWeight = SemiBold,
        color = if (account.name != null) colors.tableText else colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )

      if (isLinked) {
        Text(
          text = account.bankName ?: Strings.bankSyncUnknownBank,
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
          maxLines = 1,
          overflow = Ellipsis,
        )

        Text(
          text = lastSyncText(account.lastSync),
          style = typography.bodySmall,
          color = colors.pageTextSubdued,
          maxLines = 1,
          overflow = Ellipsis,
        )
      }
    }

    account.status?.let { AccountStatusChip(it) }
  }
}

@Composable
private fun lastSyncText(lastSync: LastBankSync): String =
  when (lastSync) {
    Never -> Strings.bankSyncNeverSynced
    JustNow -> Strings.bankSyncLastSync(Strings.bankSyncJustNow)
    is MinutesAgo ->
      Strings.bankSyncLastSync(Plurals.bankSyncMinutesAgo(lastSync.minutes, lastSync.minutes))
    is HoursAgo ->
      Strings.bankSyncLastSync(Plurals.bankSyncHoursAgo(lastSync.hours, lastSync.hours))
    is DaysAgo -> Strings.bankSyncLastSync(Plurals.bankSyncDaysAgo(lastSync.days, lastSync.days))
  }

@Composable
internal fun AccountStatusChip(
  status: BankSyncAccountStatus,
  modifier: Modifier = Modifier,
) {
  val (background, text) =
    when (status) {
      Ok -> colors.noticeBackgroundLight to colors.noticeText
      Pending,
      SyncRequested -> colors.upcomingBackground to colors.upcomingText
      RateLimited,
      TimedOut,
      AttentionRequired -> colors.warningBackground to colors.warningTextDark
      Failed,
      ReauthRequired,
      AccountMissing -> colors.errorBackground to colors.errorTextDarker
      Unknown -> colors.tableRowHeaderBackground to colors.tableRowHeaderText
    }
  val label =
    when (status) {
      Ok -> Strings.bankSyncStatusOk
      Pending -> Strings.bankSyncStatusPending
      SyncRequested -> Strings.bankSyncStatusSyncRequested
      Failed -> Strings.bankSyncStatusFailed
      ReauthRequired -> Strings.bankSyncStatusReauthRequired
      AttentionRequired -> Strings.bankSyncStatusAttentionRequired
      RateLimited -> Strings.bankSyncStatusRateLimited
      TimedOut -> Strings.bankSyncStatusTimedOut
      AccountMissing -> Strings.bankSyncStatusAccountMissing
      Unknown -> Strings.bankSyncStatusUnknown
    }
  Chip(label = label, background = background, text = text, modifier = modifier)
}

@Composable
internal fun ProviderStatusChip(
  status: BankSyncProviderStatus,
  modifier: Modifier = Modifier,
) {
  val (background, text) =
    when (status) {
      Configured -> colors.noticeBackgroundLight to colors.noticeText
      NotConfigured -> colors.warningBackground to colors.warningTextDark
      Failed -> colors.errorBackground to colors.errorTextDarker
      Checking,
      NoServer -> colors.tableRowHeaderBackground to colors.tableRowHeaderText
    }
  val label =
    when (status) {
      Checking -> Strings.bankSyncProviderChecking
      Configured -> Strings.bankSyncProviderConfigured
      NotConfigured -> Strings.bankSyncProviderNotConfigured
      Failed -> Strings.bankSyncProviderFailed
      NoServer -> Strings.bankSyncProviderNoServer
    }
  Chip(label, background, text, modifier)
}

@Composable
private fun Chip(
  label: String,
  background: Color,
  text: Color,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.background(background, CardShape).padding(BankSyncDS.chipPadding)) {
    Text(text = label, style = typography.labelSmall, color = text, maxLines = 1)
  }
}

@Composable
internal fun ProviderHeader(
  source: AccountSyncSource,
  status: BankSyncProviderStatus,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(BankSyncDS.headerPadding),
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.headerSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = providerName(source),
      style = typography.titleSmall,
      fontWeight = SemiBold,
      color = colors.pageTextLight,
      maxLines = 1,
      overflow = Ellipsis,
    )
    ProviderStatusChip(status)
  }
}

@Composable
internal fun UnlinkedHeader(modifier: Modifier = Modifier) {
  Column(modifier = modifier.fillMaxWidth().padding(BankSyncDS.headerPadding)) {
    Text(
      text = Strings.bankSyncUnlinkedTitle,
      style = typography.titleSmall,
      fontWeight = SemiBold,
      color = colors.pageTextLight,
    )
    Text(
      text = Strings.bankSyncUnlinkedMessage,
      style = typography.bodySmall,
      color = colors.pageTextSubdued,
    )
  }
}

@Composable
internal fun ReadOnlyNotice(modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.noticeBackgroundLight, RowShape)
        .padding(BankSyncDS.noticePadding),
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.noticeSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(BankSyncDS.noticeIconSize),
      imageVector = MaterialIcons.AccountBalance,
      contentDescription = null,
      tint = colors.noticeText,
    )
    Text(
      text = Strings.bankSyncReadOnly,
      style = typography.bodySmall,
      color = colors.noticeText,
    )
  }
}

// Brand names, see getSyncSourceReadable() in
// packages/desktop-client/src/components/banksync/bankSyncUtils.ts
@Composable
private fun providerName(source: AccountSyncSource): String =
  when (source) {
    GoCardless -> Strings.bankSyncProviderGocardless
    SimpleFin -> Strings.bankSyncProviderSimplefin
    PluggyAi -> Strings.bankSyncProviderPluggyai
    EnableBanking -> Strings.bankSyncProviderEnableBanking
    Akahu -> Strings.bankSyncProviderAkahu
    else -> source.value
  }

/** Keep in sync with [BankSyncAccountItem] */
@Composable
internal fun ShimmerBankSyncAccountItem(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .padding(BankSyncDS.itemCardPadding)
        .shimmer(shimmer),
    horizontalArrangement = Arrangement.spacedBy(BankSyncDS.itemHorizontalSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(BankSyncDS.itemContentSpacing),
    ) {
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.45f)
            .height(BankSyncDS.shimmerItemTextHeight)
            .background(colors.pageText, CardShape)
      )
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.35f)
            .height(BankSyncDS.shimmerItemTextHeightSmall)
            .background(colors.pageText, CardShape)
      )
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.6f)
            .height(BankSyncDS.shimmerItemTextHeightSmall)
            .background(colors.pageText, CardShape)
      )
    }

    Box(
      modifier =
        Modifier.height(BankSyncDS.shimmerItemTextHeight)
          .width(BankSyncDS.shimmerChipWidth)
          .background(colors.pageText, CardShape)
    )
  }
}

@Preview
@Composable
private fun PreviewAccountItem(
  @PreviewParameter(AccountProvider::class) params: ColoredParams<BankSyncAccount>
) = PreviewWithColoredParams(params) { BankSyncAccountItem(account = this, isLinked = true) }

private class AccountProvider :
  ColoredParameterProvider<BankSyncAccount>(
    BankSyncPreview.checking,
    BankSyncPreview.savings,
    BankSyncPreview.creditCard,
  )

@Preview
@Composable
private fun PreviewUnlinkedAccountItem(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    BankSyncAccountItem(account = BankSyncPreview.cash, isLinked = false)
  }

@Preview
@Composable
private fun PreviewAccountStatusChip(
  @PreviewParameter(AccountStatusProvider::class) params: ColoredParams<BankSyncAccountStatus>
) = PreviewWithColoredParams(params) { AccountStatusChip(this) }

private class AccountStatusProvider :
  ColoredParameterProvider<BankSyncAccountStatus>(BankSyncAccountStatus.entries)

@Preview
@Composable
private fun PreviewProviderHeader(
  @PreviewParameter(ProviderStatusProvider::class) params: ColoredParams<BankSyncProviderStatus>
) =
  PreviewWithColoredParams(params) {
    ProviderHeader(source = GoCardless, status = this)
  }

private class ProviderStatusProvider :
  ColoredParameterProvider<BankSyncProviderStatus>(BankSyncProviderStatus.entries)

@Preview
@Composable
private fun PreviewReadOnlyNotice(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ReadOnlyNotice() }

@Preview
@Composable
private fun PreviewLoadingItem(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerBankSyncAccountItem() }
