package aktual.prefs.vm.root

import aktual.prefs.vm.FakeNightlyUpdates
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest

class SettingsViewModelTest {
  @Test
  fun `Budget name follows the open budget`() = runTest {
    val openBudget = MutableStateFlow<String?>(null)
    val viewModel = SettingsViewModel({ openBudget }, FakeNightlyUpdates(), viewModelContext)

    viewModel.budgetName.test {
      assertThatNextEmissionIsEqualTo(null)

      openBudget.update { "My Budget" }
      assertThatNextEmissionIsEqualTo("My Budget")

      openBudget.update { null }
      assertThatNextEmissionIsEqualTo(null)
    }
  }
}
