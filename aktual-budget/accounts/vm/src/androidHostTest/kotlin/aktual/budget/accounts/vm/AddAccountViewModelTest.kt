package aktual.budget.accounts.vm

import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

class AddAccountViewModelTest {
  @Test
  fun `Can bank sync with a server`() {
    val server = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("token"))
    assertThat(AddAccountViewModel(server).canBankSync).isTrue()
  }

  @Test
  fun `Can't bank sync without a server`() {
    assertThat(AddAccountViewModel(None).canBankSync).isFalse()
  }
}
