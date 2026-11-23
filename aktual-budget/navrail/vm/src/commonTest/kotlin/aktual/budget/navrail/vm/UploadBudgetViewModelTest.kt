package aktual.budget.navrail.vm

import aktual.budget.navrail.vm.UploadBudgetState.Failed
import aktual.budget.utils.UploadResult
import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class UploadBudgetViewModelTest {
  private val result = CompletableDeferred<UploadResult>()

  @Test
  fun `Shows progress, then success`() = runTest {
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(Idle)

      viewModel.upload()
      assertThatNextEmissionIsEqualTo(Uploading)

      result.complete(Success)
      assertThatNextEmissionIsEqualTo(Done)
    }
  }

  @Test
  fun `Shows the failure until reset`() = runTest {
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(Idle)

      viewModel.upload()
      assertThatNextEmissionIsEqualTo(Uploading)

      result.complete(Unreachable)
      assertThatNextEmissionIsEqualTo(Failed(Unreachable))

      viewModel.reset()
      assertThatNextEmissionIsEqualTo(Idle)
    }
  }

  @Test
  fun `Server host`() = runTest {
    assertThat(viewModel().serverHost).isEqualTo("test.server.com")
  }

  private fun TestScope.viewModel() =
    UploadBudgetViewModel(
      uploader = { result.await() },
      server = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("token")),
      viewModelContext = viewModelContext,
    )
}
