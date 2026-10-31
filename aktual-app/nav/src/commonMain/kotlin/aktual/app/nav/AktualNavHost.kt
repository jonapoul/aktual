package aktual.app.nav

import aktual.core.nav.NavEntryContributor
import aktual.core.nav.NavStack
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.collections.immutable.ImmutableSet

@Composable
fun AktualNavHost(
  navStack: NavStack<NavKey>,
  contributors: ImmutableSet<NavEntryContributor>,
  modifier: Modifier = Modifier,
) {
  NavDisplay(
    backStack = navStack,
    modifier = modifier,
    onBack = { navStack.pop() },
    transitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> -width })
    },
    popTransitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> width })
    },
    predictivePopTransitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> width })
    },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    entryProvider =
      entryProvider {
        for (contributor in contributors) {
          with(contributor) { contribute(navStack) }
        }
      },
  )
}
