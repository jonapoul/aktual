package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.CategoryOrder
import aktual.budget.budgeting.domain.CategoryWriter
import aktual.budget.budgeting.domain.NameResult
import aktual.budget.budgeting.domain.OrderedCategory
import aktual.budget.budgeting.domain.OrderedGroup
import aktual.budget.budgeting.domain.UndoToken
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.di.BudgetCoroutineScope
import aktual.test.unconfinedViewModelContext
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class CategoryEditViewModelTest {
  private val writer = FakeCategoryWriter()
  private val budgetWriter = RecordingBudgetWriter()

  @Test
  fun `Adding lists every group`() = runEditTest { viewModel ->
    viewModel.startAdd()

    assertThat(viewModel.dialog.value)
      .isEqualTo(
        CategoryDialog.Add(
          persistentListOf(
            GroupChoice(USUAL, "Usual", isIncome = false),
            GroupChoice(INCOME, "Income", isIncome = true),
          ),
        ),
      )
  }

  @Test
  fun `Creating a category closes the dialog`() = runEditTest { viewModel ->
    viewModel.startAdd()
    viewModel.createCategory(USUAL, "Rent")

    assertThat(writer.calls).containsExactly(listOf("createCategory", USUAL, "Rent"))
    assertThat(viewModel.dialog.value).isNull()
  }

  @Test
  fun `Creating a group`() = runEditTest { viewModel ->
    viewModel.startAdd()
    viewModel.createGroup("Bills")

    assertThat(writer.calls).containsExactly(listOf("createGroup", "Bills"))
  }

  @Test
  fun `A duplicate name keeps the dialog open until the name changes`() = runEditTest { viewModel ->
    writer.nameResult = Duplicate
    viewModel.startRename(CategoryTarget.Category(FOOD, "Food"))
    viewModel.rename("Fun")

    assertThat(viewModel.dialog.value)
      .isEqualTo(
        CategoryDialog.Rename(CategoryTarget.Category(FOOD, "Food"), Duplicate),
      )

    viewModel.edited()
    assertThat(viewModel.dialog.value?.status).isEqualTo(Editing)
  }

  @Test
  fun `Renaming a group`() = runEditTest { viewModel ->
    viewModel.startRename(CategoryTarget.Group(USUAL, "Usual"))
    viewModel.rename("Everyday")

    assertThat(writer.calls).containsExactly(listOf("renameGroup", USUAL, "Everyday"))
    assertThat(viewModel.dialog.value).isNull()
  }

  @Test
  fun `A failed save keeps the dialog open`() = runEditTest { viewModel ->
    writer.fail = true
    viewModel.startRename(CategoryTarget.Group(USUAL, "Usual"))
    viewModel.rename("Everyday")

    assertThat(viewModel.dialog.value?.status).isEqualTo(Failed)
  }

  @Test
  fun `Deleting an unused category needs no transfer`() = runEditTest { viewModel ->
    viewModel.startDelete(CategoryTarget.Category(FOOD, "Food"))

    assertThat(viewModel.dialog.value)
      .isNotNull()
      .isInstanceOf<CategoryDialog.Delete>()
      .prop(CategoryDialog.Delete::transfer)
      .isEqualTo(NotNeeded)
  }

  @Test
  fun `A used category offers the other categories of the same kind`() = runEditTest { viewModel ->
    writer.transferRequired = true
    viewModel.startDelete(CategoryTarget.Category(FOOD, "Food"))

    assertThat(viewModel.dialog.value)
      .isNotNull()
      .isInstanceOf<CategoryDialog.Delete>()
      .prop(CategoryDialog.Delete::transfer)
      .isEqualTo(
        Transfer.Needed(
          persistentListOf(
            TransferGroup("Usual", persistentListOf(TransferCategory(FUN, "Fun"))),
          ),
        ),
      )
  }

  @Test
  fun `A group with no categories left elsewhere offers nothing`() = runEditTest { viewModel ->
    writer.transferRequired = true
    viewModel.startDelete(CategoryTarget.Group(USUAL, "Usual"))

    assertThat(viewModel.dialog.value)
      .isNotNull()
      .isInstanceOf<CategoryDialog.Delete>()
      .prop(CategoryDialog.Delete::transfer)
      .isEqualTo(Transfer.Needed(persistentListOf()))
  }

  @Test
  fun `Deleting can't go ahead without a needed transfer`() = runEditTest { viewModel ->
    writer.transferRequired = true
    viewModel.startDelete(CategoryTarget.Category(FOOD, "Food"))
    viewModel.delete(transferTo = null)

    assertThat(writer.calls.filter { it.first() == "deleteCategory" }).isEmpty()
  }

  @Test
  fun `Deleting emits a token that can be undone once`() = runEditTest { viewModel ->
    writer.transferRequired = true
    viewModel.events.test {
      viewModel.startDelete(CategoryTarget.Category(FOOD, "Food"))
      viewModel.delete(transferTo = FUN)

      val event = awaitItem()
      assertThat(event).isInstanceOf<CategoryEditEvent.Deleted>()
      assertThat(writer.calls.last()).isEqualTo(listOf("deleteCategory", FOOD, FUN))
      assertThat(viewModel.dialog.value).isNull()

      val token = event.token
      viewModel.undo(token)
      viewModel.undo(token)
      runCurrent()
      assertThat(budgetWriter.calls).containsExactly(listOf("undo", token))
    }
  }

  @Test
  fun `Hiding a group emits an update`() = runEditTest { viewModel ->
    viewModel.events.test {
      viewModel.setGroupHidden(USUAL, hidden = true)

      assertThat(awaitItem()).isInstanceOf<CategoryEditEvent.Updated>()
      assertThat(writer.calls).containsExactly(listOf("setGroupHidden", USUAL, true))
    }
  }

  private fun runEditTest(action: suspend TestScope.(CategoryEditViewModel) -> Unit) = runTest {
    val viewModel =
      CategoryEditViewModel(
        categoryWriter = writer,
        budgetWriter = budgetWriter,
        order = FakeCategoryOrder,
        budgetScope = BudgetCoroutineScope(backgroundScope),
        viewModelContext = unconfinedViewModelContext,
      )
    action(viewModel)
  }

  private class FakeCategoryWriter : CategoryWriter {
    val calls = mutableListOf<List<Any?>>()
    var nameResult: NameResult<Unit> = NameResult.Saved(Unit)
    var transferRequired = false
    var fail = false

    override suspend fun createCategory(group: CategoryGroupId, name: String) =
      named("createCategory", group, name) { CategoryId("new") }

    override suspend fun createGroup(name: String) =
      named("createGroup", name) { CategoryGroupId("new") }

    override suspend fun renameCategory(id: CategoryId, name: String) =
      named("renameCategory", id, name) {}

    override suspend fun renameGroup(id: CategoryGroupId, name: String) =
      named("renameGroup", id, name) {}

    override suspend fun setGroupHidden(id: CategoryGroupId, hidden: Boolean) =
      record("setGroupHidden", id, hidden)

    override suspend fun isTransferRequired(categories: Collection<CategoryId>) = transferRequired

    override suspend fun deleteCategory(id: CategoryId, transferTo: CategoryId?) =
      record("deleteCategory", id, transferTo)

    override suspend fun deleteGroup(id: CategoryGroupId, transferTo: CategoryId?) =
      record("deleteGroup", id, transferTo)

    private fun <T> named(vararg call: Any?, value: () -> T): NameResult<T> {
      check(!fail) { "Write failed" }
      calls += call.toList()
      return when (nameResult) {
        is Saved -> NameResult.Saved(value())
        Blank -> Blank
        Duplicate -> Duplicate
      }
    }

    private fun record(vararg call: Any?): UndoToken {
      check(!fail) { "Write failed" }
      calls += call.toList()
      return UndoToken(emptyList())
    }
  }

  private object FakeCategoryOrder : CategoryOrder {
    override suspend fun load() =
      listOf(
        OrderedGroup(
          id = USUAL,
          name = "Usual",
          isIncome = false,
          isHidden = false,
          categories =
            listOf(OrderedCategory(FOOD, "Food", false), OrderedCategory(FUN, "Fun", false)),
        ),
        OrderedGroup(
          id = INCOME,
          name = "Income",
          isIncome = true,
          isHidden = false,
          categories = listOf(OrderedCategory(SALARY, "Salary", false)),
        ),
      )

    override suspend fun save(groups: List<OrderedGroup>) = Unit
  }

  private companion object {
    val USUAL = CategoryGroupId("usual")
    val INCOME = CategoryGroupId("income")
    val FOOD = CategoryId("food")
    val FUN = CategoryId("fun")
    val SALARY = CategoryId("salary")
  }
}
