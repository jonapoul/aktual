package aktual.about.ui.licenses

import aktual.about.vm.LicenseSorting
import aktual.core.icons.material.AccountBalance
import aktual.core.icons.material.Badge
import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.ListBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun LicenseSortingBottomSheet(
  value: LicenseSorting,
  onAction: LicensesActionHandler,
  sheetState: SheetState,
  modifier: Modifier = Modifier,
) {
  ListBottomSheet(
    modifier = modifier,
    value = value,
    options = LicenseSorting.entries.toImmutableList(),
    onDismiss = { onAction(DismissSortSheet) },
    onSelect = { onAction(SetSorting(it)) },
    string = { it.string() },
    leadingContent = { BottomSheetIcon(it.icon()) },
    sheetState = sheetState,
    key = { it.ordinal },
  )
}

@Composable
private fun LicenseSorting.string(): String =
  when (this) {
    ByArtifact -> Strings.licensesSortByArtifact
    ByName -> Strings.licensesSortByName
    ByLicense -> Strings.licensesSortByLicense
  }

@Stable
private fun LicenseSorting.icon(): ImageVector =
  when (this) {
    ByArtifact -> MaterialIcons.FormatListBulleted
    ByName -> MaterialIcons.Badge
    ByLicense -> MaterialIcons.AccountBalance
  }
