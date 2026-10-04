package aktual.budget.home.vm

import aktual.budget.model.DbMetadata
import aktual.test.TestBudgetLocalPreferences
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {
  @BeforeTest
  fun before() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Budget name follows local preferences`() = runTest {
    val prefs = TestBudgetLocalPreferences(DbMetadata(budgetName = "Household"))
    val viewModel = HomeViewModel(prefs)

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(HomeState(budgetName = "Household"))

      prefs += DbMetadata(budgetName = "Renamed")
      assertThat(awaitItem()).isEqualTo(HomeState(budgetName = "Renamed"))
    }
  }
}
