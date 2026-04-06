package aktual.app.nav

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
  navStack: AktualNavStack<NavKey>,
  contributors: ImmutableSet<NavEntryContributor>,
  modifier: Modifier = Modifier,
) {
  NavDisplay(
    backStack = navStack,
    modifier = modifier,
    onBack = { navStack.pop() },
    // Forward: new screen slides in from right, old screen slides out to left
    transitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> -width })
    },
    // Back: previous screen slides in from left, current screen slides out to right
    popTransitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> width })
    },
    // Predictive back gesture: same as pop
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
          contributor.contribute(scope = this, navStack)
        }
      },
  )
}
