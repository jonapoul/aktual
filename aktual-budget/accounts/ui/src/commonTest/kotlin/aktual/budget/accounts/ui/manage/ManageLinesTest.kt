package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.vm.manage.ManagedAccount
import aktual.budget.accounts.vm.manage.ManagedGroup
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Amount.Companion.Zero
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class ManageLinesTest {
  @Test
  fun `Empty groups show an empty line, and collapsed groups only headers`() {
    assertThat(keys(GROUPS))
      .containsExactly(
        "on-group-null",
        "on-account-wallet",
        "on-group-everyday",
        "on-account-current",
        "on-account-credit",
        "on-group-savings",
        "on-group-savings-empty",
      )
    assertThat(manageLines(OnBudget, GROUPS, collapsed = true).map { it.key })
      .containsExactly("on-group-null", "on-group-everyday", "on-group-savings")
  }

  @Test
  fun `An account moves within its group`() {
    val moved = movedAccount(OnBudget, GROUPS, "on-account-current", to = 4)

    assertThat(moved)
      .isNotNull()
      .transform { it.accountIds() }
      .containsExactly(listOf(WALLET.id), listOf(CREDIT.id, CURRENT.id), emptyList<AccountId>())
  }

  @Test
  fun `Moving an account past the next header puts it first in that group`() {
    val moved = movedAccount(OnBudget, GROUPS, "on-account-wallet", to = 2)

    assertThat(moved)
      .isNotNull()
      .transform { it.accountIds() }
      .containsExactly(
        emptyList<AccountId>(),
        listOf(WALLET.id, CURRENT.id, CREDIT.id),
        emptyList<AccountId>(),
      )
  }

  @Test
  fun `Moving an account above its header puts it last in the group before`() {
    val moved = movedAccount(OnBudget, GROUPS, "on-account-current", to = 2)

    assertThat(moved)
      .isNotNull()
      .transform { it.accountIds() }
      .containsExactly(listOf(WALLET.id, CURRENT.id), listOf(CREDIT.id), emptyList<AccountId>())
  }

  @Test
  fun `An account dropped on an empty group joins it, updating the totals`() {
    val moved = movedAccount(OnBudget, GROUPS, "on-account-credit", to = 6)

    assertThat(moved)
      .isNotNull()
      .transform { it.accountIds() }
      .containsExactly(listOf(WALLET.id), listOf(CURRENT.id), listOf(CREDIT.id))
    assertThat(moved?.map { it.total })
      .isNotNull()
      .containsExactly(Amount(20.0), Amount(100.0), Amount(-50.0))
  }

  @Test
  fun `An account can't go above Ungrouped`() {
    assertThat(movedAccount(OnBudget, GROUPS, "on-account-wallet", to = 0)).isNull()
  }

  @Test
  fun `Group lines don't move as accounts`() {
    assertThat(movedAccount(OnBudget, GROUPS, "on-group-everyday", to = 0)).isNull()
  }

  @Test
  fun `A group moves among the headers`() {
    val moved = movedGroup(OnBudget, GROUPS, "on-group-savings", to = 1)

    assertThat(moved?.map { it.id }).isEqualTo(listOf(null, SAVINGS, EVERYDAY))
  }

  @Test
  fun `Ungrouped stays first`() {
    assertThat(movedGroup(OnBudget, GROUPS, "on-group-everyday", to = 0)).isNull()
    assertThat(movedGroup(OnBudget, GROUPS, "on-group-null", to = 1)).isNull()
  }

  @Test
  fun `The other side follows a group's new order`() {
    val other = GROUPS.map { it.copy(accounts = persistentListOf()) }
    val order = movedGroup(OnBudget, GROUPS, "on-group-savings", to = 1)!!

    assertThat(other.inOrderOf(order).map { it.id }).isEqualTo(listOf(null, SAVINGS, EVERYDAY))
  }

  private fun keys(groups: List<ManagedGroup>) = manageLines(OnBudget, groups).map { it.key }

  private fun List<ManagedGroup>.accountIds() = map { g -> g.accounts.map { it.id } }

  private companion object {
    val EVERYDAY = AccountGroupId("everyday")
    val SAVINGS = AccountGroupId("savings")

    val WALLET = account("wallet", 20.0, group = null)
    val CURRENT = account("current", 100.0, EVERYDAY)
    val CREDIT = account("credit", -50.0, EVERYDAY)

    val GROUPS =
      listOf(
        group(null, WALLET),
        group(EVERYDAY, CURRENT, CREDIT),
        group(SAVINGS),
      )

    fun account(id: String, balance: Double, group: AccountGroupId?) =
      ManagedAccount(
        id = AccountId(id),
        name = id,
        balance = Amount(balance),
        transactionCount = 0,
        hasNotes = false,
        offBudget = false,
        group = group,
      )

    fun group(id: AccountGroupId?, vararg accounts: ManagedAccount) =
      ManagedGroup(
        id = id,
        name = id?.value.orEmpty(),
        total = accounts.fold(Zero) { sum, a -> sum + a.balance },
        accounts = accounts.toList().toImmutableList(),
      )
  }
}
