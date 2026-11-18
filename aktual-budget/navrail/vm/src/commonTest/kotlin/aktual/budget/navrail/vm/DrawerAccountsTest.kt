package aktual.budget.navrail.vm

import aktual.budget.db.GetAllWithBalances
import aktual.budget.db.accountGroups.GetAllActive
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test

class DrawerAccountsTest {
  @Test
  fun `Ungrouped accounts come first, then groups in their order`() {
    val accounts =
      listOf(row("a", group = "second"), row("b"), row("c", group = "first"), row("d"))
        .toDrawerAccounts(groups = listOf(group("first"), group("second")))

    assertThat(accounts.onBudget.buckets.map { it.group?.name to it.names() })
      .containsExactly(
        null to listOf("b", "d"),
        "first" to listOf("c"),
        "second" to listOf("a"),
      )
  }

  @Test
  fun `Accounts in a missing group are ungrouped`() {
    val accounts = listOf(row("a", group = "gone"), row("b")).toDrawerAccounts()

    assertThat(accounts.onBudget.buckets.map { it.group to it.names() })
      .containsExactly(null to listOf("a", "b"))
  }

  @Test
  fun `Groups without accounts on a side are left out of it`() {
    val accounts =
      listOf(row("on", group = "g"), row("off", offBudget = true))
        .toDrawerAccounts(groups = listOf(group("g")))

    assertThat(accounts.onBudget.buckets.map { it.group?.name }).containsExactly("g")
    assertThat(accounts.offBudget.buckets.map { it.group?.name }).containsExactly(null)
  }

  @Test
  fun `Closed accounts aren't grouped`() {
    val accounts =
      listOf(row("a", group = "g", closed = true)).toDrawerAccounts(groups = listOf(group("g")))

    assertThat(accounts.closed.buckets.map { it.group to it.names() })
      .containsExactly(null to listOf("a"))
  }

  @Test
  fun `Sections and buckets count failed syncs and sum balances`() {
    val accounts =
      listOf(
          row("a", balance = 100, failed = true),
          row("b", balance = 50, group = "g", failed = true),
          row("c", balance = -20, group = "g"),
        )
        .toDrawerAccounts(groups = listOf(group("g")))

    assertThat(accounts.onBudget).all {
      prop(DrawerAccountSection::total).isEqualTo(Amount(130L))
      prop(DrawerAccountSection::accountCount).isEqualTo(3)
      prop(DrawerAccountSection::failedCount).isEqualTo(2)
    }
    assertThat(accounts.onBudget.buckets.map { Triple(it.group?.name, it.total, it.failedCount) })
      .containsExactly(Triple(null, Amount(100L), 1), Triple("g", Amount(30L), 1))
  }

  @Test
  fun `Searching matches account or group names and keeps totals`() {
    val accounts =
      listOf(
          row("Current"),
          row("Joint", group = "Household"),
          row("Bills", group = "Household"),
          row("Savings", balance = 10),
          row("Old current", closed = true),
        )
        .toDrawerAccounts(groups = listOf(group("Household")))

    val byAccount = accounts.filter(" current ")
    assertThat(byAccount.onBudget.names()).containsExactly("Current")
    assertThat(byAccount.closed.names()).containsExactly("Old current")
    assertThat(byAccount.onBudget).all {
      prop(DrawerAccountSection::total).isEqualTo(accounts.onBudget.total)
      prop(DrawerAccountSection::accountCount).isEqualTo(4)
    }

    assertThat(accounts.filter("house").onBudget.buckets.map { it.group?.name to it.names() })
      .containsExactly("Household" to listOf("Joint", "Bills"))

    assertThat(accounts.filter("nothing").onBudget.buckets).isEmpty()
    assertThat(accounts.filter("  ")).isEqualTo(accounts)
  }

  @Test
  fun `Expand all covers sides and groups but not closed accounts`() {
    val accounts =
      listOf(row("a", group = "g"), row("b", group = "g", offBudget = true), row("c"))
        .toDrawerAccounts(groups = listOf(group("g")))

    assertThat(accounts.toggleableSections().map { it.key })
      .containsExactly("onbudget", "offbudget", "group-g-on", "group-g-off")
  }

  private fun DrawerAccountBucket.names() = accounts.map { it.name }

  private fun DrawerAccountSection.names() = accounts.map { it.name }

  private fun group(name: String) = GetAllActive(AccountGroupId(name), name, sort_order = null)

  private fun row(
    name: String,
    balance: Long = 0,
    group: String? = null,
    offBudget: Boolean = false,
    closed: Boolean = false,
    failed: Boolean = false,
  ) =
    GetAllWithBalances(
      id = AccountId(name),
      name = name,
      offbudget = offBudget,
      closed = closed,
      account_sync_source = if (failed) AccountSyncSource.SimpleFin else null,
      account_id = if (failed) "remote-$name" else null,
      bank_sync_status = if (failed) BankSyncStatus.ReauthRequired else null,
      last_sync = null,
      balance = balance,
      last_activity = null,
      account_group_id = group?.let(::AccountGroupId),
    )
}
