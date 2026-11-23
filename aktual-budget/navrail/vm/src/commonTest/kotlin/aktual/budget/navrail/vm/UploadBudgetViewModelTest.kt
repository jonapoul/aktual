package aktual.budget.navrail.vm

import aktual.budget.utils.BudgetUploader
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
      assertThat(awaitItem()).isEqualTo(UploadBudgetState.Idle)

      viewModel.upload()
      assertThatNextEmissionIsEqualTo(UploadBudgetState.Uploading)

      result.complete(UploadResult.Success)
      assertThatNextEmissionIsEqualTo(UploadBudgetState.Done)
    }
  }

  @Test
  fun `Shows the failure until reset`() = runTest {
    val viewModel = viewModel()

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(UploadBudgetState.Idle)

      viewModel.upload()
      assertThatNextEmissionIsEqualTo(UploadBudgetState.Uploading)

      result.complete(UploadResult.Unreachable)
      assertThatNextEmissionIsEqualTo(UploadBudgetState.Failed(UploadResult.Unreachable))

      viewModel.reset()
      assertThatNextEmissionIsEqualTo(UploadBudgetState.Idle)
    }
  }

  @Test
  fun `Server host`() = runTest {
    assertThat(viewModel().serverHost).isEqualTo("test.server.com")
  }

  private fun TestScope.viewModel() =
    UploadBudgetViewModel(
      uploader = BudgetUploader { result.await() },
      server = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("token")),
      viewModelContext = viewModelContext,
    )
}
