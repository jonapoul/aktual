package aktual.crashes.ui

import aktual.core.nav.BackNavigator
import aktual.core.nav.CrashReportNavRoute
import aktual.core.nav.CrashReportNavigator
import aktual.core.nav.CrashReportsNavRoute
import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import aktual.di.AppScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(AppScope::class)
class CrashesNavEntryContributor : NavEntryContributor {
  override fun EntryProviderScope<NavKey>.contribute(stack: NavStack<NavKey>) {
    entry<CrashReportsNavRoute> {
      CrashReportsScreen(back = BackNavigator(stack), toReport = CrashReportNavigator(stack))
    }

    entry<CrashReportNavRoute> { route ->
      CrashReportScreen(file = route.file, back = BackNavigator(stack))
    }
  }
}
