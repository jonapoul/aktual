package aktual.budget.accounts.vm

import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.viewModelContext
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddAccountViewModelTest {
  @Test
  fun `Can bank sync with a server`() = runTest {
    val server = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("token"))
    assertThat(AddAccountViewModel(server, viewModelContext).canBankSync).isTrue()
  }

  @Test
  fun `Can't bank sync without a server`() = runTest {
    assertThat(AddAccountViewModel(None, viewModelContext).canBankSync).isFalse()
  }
}
