package aktual.about.ui

import aktual.about.ui.info.InfoScreen
import aktual.about.ui.licenses.LicensesScreen
import aktual.about.ui.storage.ManageStorageScreen
import aktual.about.vm.StorageNavEvent
import aktual.core.nav.BackNavigator
import aktual.core.nav.BudgetNavRailNavRoute
import aktual.core.nav.InfoNavRoute
import aktual.core.nav.LicensesNavRoute
import aktual.core.nav.LicensesNavigator
import aktual.core.nav.ManageStorageNavRoute
import aktual.core.nav.ManageStorageNavigator
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.di.AppScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class AboutNavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: NavStack<NavKey>) {
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

  private fun adjustStackIfInvalidated(stack: NavStack<NavKey>, event: StorageNavEvent) {
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
