package aktual.about.ui.licenses

import aktual.about.vm.LicenseSorting

internal sealed interface LicensesAction

internal data object NavBack : LicensesAction

internal data object Reload : LicensesAction

internal data object OpenSearch : LicensesAction

internal data object ShowSortSheet : LicensesAction

internal data object DismissSortSheet : LicensesAction

@JvmInline internal value class SetSorting(val sorting: LicenseSorting) : LicensesAction

@JvmInline internal value class LaunchUrl(val url: String) : LicensesAction
