package aktual.budget.schedules.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.PayeeId
import aktual.budget.model.RecurConfig
import aktual.budget.schedules.vm.edit.EditScheduleState
import aktual.budget.schedules.vm.edit.NamedEntity
import aktual.budget.schedules.vm.edit.ScheduleAmount
import aktual.budget.schedules.vm.edit.ScheduleDate
import aktual.budget.schedules.vm.edit.ScheduleForm
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

internal val PreviewPayee = NamedEntity(PayeeId("landlord"), "Landlord Ltd")

internal val PreviewAccount = NamedEntity(AccountId("current"), "Current account")

internal val PreviewForm =
  ScheduleForm(
    name = "Rent",
    payee = PreviewPayee.id,
    account = PreviewAccount.id,
    amount = ScheduleAmount.Exactly(Amount(-120_000L)),
    date =
      ScheduleDate.Recurring(
        RecurConfig(
          frequency = Monthly,
          start = LocalDate(2025, 1, 1),
          interval = 1,
          skipWeekend = true,
          weekendSolveMode = Before,
          endMode = Never,
        ),
      ),
    postsTransaction = false,
  )

internal val PreviewLoaded =
  EditScheduleState.Loaded(
    form = PreviewForm,
    saved = PreviewForm,
    isNew = false,
    hasChanges = false,
    isWorking = false,
    status = Due,
    payeeName = PreviewPayee.name,
    accountName = PreviewAccount.name,
    payees = persistentListOf(PreviewPayee),
    accounts = persistentListOf(PreviewAccount),
    upcomingDates =
      persistentListOf(LocalDate(2026, 10, 1), LocalDate(2026, 10, 30), LocalDate(2026, 12, 1)),
  )

internal val PreviewDirty =
  PreviewLoaded.copy(
    form =
      PreviewForm.copy(
        amount = ScheduleAmount.Approximately(Amount(-125_000L)),
        postsTransaction = true,
      ),
    hasChanges = true,
  )

internal val PreviewOnceDate = LocalDate(2026, 11, 5)

internal val PreviewOnce =
  PreviewLoaded.copy(
    form =
      PreviewForm.copy(
        amount = ScheduleAmount.Between(Amount(4_000L), Amount(6_000L)),
        date = ScheduleDate.Once(PreviewOnceDate),
      ),
    saved =
      PreviewForm.copy(
        amount = ScheduleAmount.Between(Amount(4_000L), Amount(6_000L)),
        date = ScheduleDate.Once(PreviewOnceDate),
      ),
    status = Scheduled,
    upcomingDates = persistentListOf(PreviewOnceDate),
  )

internal val PreviewNew =
  PreviewLoaded.copy(
    form = PreviewForm.copy(name = "", payee = null),
    saved = PreviewForm.copy(name = "", payee = null),
    isNew = true,
    status = null,
    payeeName = null,
  )
