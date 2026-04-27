package aktual.about.ui

import aktual.about.ui.info.InfoScreen
import aktual.about.ui.licenses.LicensesScreen
import aktual.about.ui.storage.ManageStorageScreen
import aktual.about.vm.StorageNavEvent
import aktual.app.nav.AktualNavStack
import aktual.app.nav.BackNavigator
import aktual.app.nav.BudgetNavRailNavRoute
import aktual.app.nav.InfoNavRoute
import aktual.app.nav.LicensesNavRoute
import aktual.app.nav.LicensesNavigator
import aktual.app.nav.ManageStorageNavRoute
import aktual.app.nav.ManageStorageNavigator
import aktual.app.nav.NavEntryContributor
import aktual.app.nav.NavScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class AboutNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: AktualNavStack<NavKey>) {
    scope.entry<InfoNavRoute> {
      InfoScreen(
        back = BackNavigator(stack),
        toLicenses = LicensesNavigator(stack),
        toManageStorage = ManageStorageNavigator(stack),
      )
    }

    scope.entry<LicensesNavRoute> { LicensesScreen(BackNavigator(stack)) }

    scope.entry<ManageStorageNavRoute> {
      ManageStorageScreen(
        navBack = BackNavigator(stack),
        onStorageNavEvent = { event -> adjustStackIfInvalidated(stack, event) },
      )
    }
  }

  private fun adjustStackIfInvalidated(stack: AktualNavStack<NavKey>, event: StorageNavEvent) {
    when (event) {
      is StorageNavEvent.ActiveBudgetCleared -> {
        stack.removeIf { it is BudgetNavRailNavRoute }
      }

      is StorageNavEvent.PreferencesCleared,
      is StorageNavEvent.AllFilesCleared -> {
        // remove all backstack screens, because we don't have a token or server URL any more
        for (i in stack.lastIndex - 1 downTo 0) {
          stack.removeAt(i)
        }
      }
    }

    stack.log()
  }
}
