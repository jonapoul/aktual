package aktual.budget.home.domain

import aktual.budget.db.GetAllWithBalances
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncSource
import aktual.budget.model.Amount
import aktual.budget.model.BankSyncStatus
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test
import kotlin.time.Instant

internal class AccountsSummaryTest {
  @Test
  fun `Groups accounts and leaves closed ones out of the totals`() {
    val summary =
      listOf(
          row("a", balance = 1_000),
          row("b", balance = 250),
          row("c", balance = -5_000, offBudget = true),
          row("d", balance = 99_999, closed = true),
          row("e", balance = 77_777, closed = true, offBudget = true),
        )
        .toAccountsSummary()

    assertThat(summary).all {
      prop(AccountsSummary::onBudget).all {
        prop(AccountSection::accounts).ids().containsExactly("a", "b")
        prop(AccountSection::total).isEqualTo(Amount(1_250L))
      }
      prop(AccountsSummary::offBudget).all {
        prop(AccountSection::accounts).ids().containsExactly("c")
        prop(AccountSection::total).isEqualTo(Amount(-5_000L))
      }
      prop(AccountsSummary::closed).ids().containsExactly("d", "e")
      prop(AccountsSummary::netWorth).isEqualTo(Amount(-3_750L))
    }
  }

  @Test
  fun `Empty list gives zero totals`() {
    assertThat(emptyList<GetAllWithBalances>().toAccountsSummary()).all {
      prop(AccountsSummary::onBudget).prop(AccountSection::total).isEqualTo(Zero)
      prop(AccountsSummary::offBudget).prop(AccountSection::total).isEqualTo(Zero)
      prop(AccountsSummary::netWorth).isEqualTo(Zero)
    }
  }

  @Test
  fun `Maps sync state`() {
    val states =
      listOf(
          row("unlinked", syncSource = null, status = Failed),
          row("no-remote-id", remoteId = null),
          row("never-synced"),
          row("ok", status = Ok, lastSync = LAST_SYNC),
          row("pending", status = Pending, lastSync = LAST_SYNC),
          row("failed", status = Failed),
          row("reauth", status = ReauthRequired),
          row("attention", status = AttentionRequired),
          row("missing", status = AccountMissing),
          row("timeout", status = TimedOut),
          row("rate-limit", status = RateLimitExceeded),
        )
        .toAccountsSummary()
        .onBudget
        .accounts
        .map { it.syncState }

    assertThat(states)
      .containsExactly(
        AccountSyncState.NotLinked,
        AccountSyncState.NotLinked,
        AccountSyncState.Ok(lastSync = null),
        AccountSyncState.Ok(LAST_SYNC),
        AccountSyncState.Ok(LAST_SYNC),
        AccountSyncState.Failed(Failed),
        AccountSyncState.Failed(ReauthRequired),
        AccountSyncState.Failed(AttentionRequired),
        AccountSyncState.Failed(AccountMissing),
        AccountSyncState.Failed(TimedOut),
        AccountSyncState.Failed(RateLimitExceeded),
      )
  }

  private fun Assert<List<AccountBalance>>.ids() = transform { accounts ->
    accounts.map { it.id.value }
  }

  private fun row(
    id: String,
    balance: Long = 0,
    offBudget: Boolean = false,
    closed: Boolean = false,
    syncSource: AccountSyncSource? = GoCardless,
    remoteId: String? = "remote-$id",
    status: BankSyncStatus? = null,
    lastSync: Instant? = null,
  ) =
    GetAllWithBalances(
      id = AccountId(id),
      name = id,
      offbudget = offBudget,
      closed = closed,
      account_sync_source = syncSource,
      account_id = remoteId,
      bank_sync_status = status,
      last_sync = lastSync,
      balance = balance,
    )

  private companion object {
    val LAST_SYNC = Instant.parse("2026-01-01T12:00:00Z")
  }
}
