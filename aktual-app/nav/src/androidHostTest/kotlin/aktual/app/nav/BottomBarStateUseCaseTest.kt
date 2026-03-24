package aktual.app.nav

import aktual.core.di.BudgetGraphHolder
import aktual.core.model.PingState.Failure
import aktual.core.model.PingState.Success
import aktual.core.model.PingState.Unknown
import aktual.core.model.PingStateHolder
import aktual.core.prefs.SystemUiPreferences
import aktual.core.prefs.SystemUiPreferencesImpl
import aktual.core.ui.BottomBarState.Hidden
import aktual.core.ui.BottomBarState.Visible
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BottomBarStateUseCaseTest {
  private lateinit var preferences: SystemUiPreferences
  private lateinit var pingStateHolder: PingStateHolder
  private lateinit var useCase: BottomBarStateUseCase

  private fun runUseCaseTest(testBody: suspend TestScope.() -> Unit): TestResult = runTest {
    preferences = SystemUiPreferencesImpl(buildPreferences())
    pingStateHolder = PingStateHolder()
    val budgetGraphHolder =
      mockk<BudgetGraphHolder>(relaxed = true) { every { value } returns null }
    useCase = BottomBarStateUseCase(preferences, budgetGraphHolder, pingStateHolder)
    testBody()
  }

  @Test
  fun `Default state is visible with unknown ping`() = runUseCaseTest {
    useCase(backgroundScope).test {
      assertThatNextEmissionIsEqualTo(Visible(pingState = Unknown, budgetName = null))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Hidden when showBottomBar is set to false`() = runUseCaseTest {
    useCase(backgroundScope).test {
      assertThatNextEmissionIsEqualTo(Visible(pingState = Unknown, budgetName = null))
      preferences.showBottomBar.set(false)
      assertThatNextEmissionIsEqualTo(Hidden)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Reflects ping state changes`() = runUseCaseTest {
    useCase(backgroundScope).test {
      assertThatNextEmissionIsEqualTo(Visible(pingState = Unknown, budgetName = null))
      pingStateHolder.update { Success }
      assertThatNextEmissionIsEqualTo(Visible(pingState = Success, budgetName = null))
      pingStateHolder.update { Failure }
      assertThatNextEmissionIsEqualTo(Visible(pingState = Failure, budgetName = null))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Toggles visibility when preference changes`() = runUseCaseTest {
    useCase(backgroundScope).test {
      assertThatNextEmissionIsEqualTo(Visible(pingState = Unknown, budgetName = null))
      preferences.showBottomBar.set(false)
      assertThatNextEmissionIsEqualTo(Hidden)
      preferences.showBottomBar.set(true)
      assertThatNextEmissionIsEqualTo(Visible(pingState = Unknown, budgetName = null))
      cancelAndIgnoreRemainingEvents()
    }
  }
}
