package aktual.budget.home.vm

import aktual.core.model.BudgetServer
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import aktual.test.TestCalendar
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class HomeViewModelBankSyncTest {
  @Test
  fun `Can bank sync with a server`() = runDatabaseTest { scope ->
    val server = BudgetServer.Remote(ServerUrl(Https, "test.server.com"), Token("token"))
    val viewModel = createHomeViewModel(scope, CALENDAR, server = server)
    assertThat(viewModel.canBankSync).isTrue()
  }

  @Test
  fun `Can't bank sync without a server`() = runDatabaseTest { scope ->
    val viewModel = createHomeViewModel(scope, CALENDAR)
    assertThat(viewModel.canBankSync).isFalse()
  }

  private companion object {
    val CALENDAR = TestCalendar(LocalDate(2026, 4, 1))
  }
}
