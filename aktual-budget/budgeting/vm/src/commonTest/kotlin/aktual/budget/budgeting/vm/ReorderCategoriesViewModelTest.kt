package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.CategoryOrder
import aktual.budget.budgeting.domain.OrderedCategory
import aktual.budget.budgeting.domain.OrderedGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.unconfinedViewModelContext
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class ReorderCategoriesViewModelTest {
  private val order = FakeCategoryOrder()

  @Test
  fun `Loads with no changes`() = runReorderTest { viewModel ->
    assertThat(viewModel.state.value)
      .isEqualTo(ReorderCategoriesState.Loaded(LOADED, hasChanges = false, isSaving = false))
  }

  @Test
  fun `Moving a category within its group`() = runReorderTest { viewModel ->
    viewModel.moveCategory(FOOD.id, BILLS.id, index = 0)
    viewModel.moveCategory(RENT.id, USUAL.id, index = 1)

    assertThat(viewModel.loaded()).all {
      prop(ReorderCategoriesState.Loaded::hasChanges).isTrue()
      prop(ReorderCategoriesState.Loaded::groups)
        .transform { groups -> groups.map { g -> g.categories.map { it.id } } }
        .containsExactly(listOf(FUN.id, RENT.id), listOf(FOOD.id), listOf(SALARY.id))
    }
  }

  @Test
  fun `Moving back to the loaded order has no changes`() = runReorderTest { viewModel ->
    viewModel.moveCategory(FOOD.id, USUAL.id, index = 1)
    assertThat(viewModel.loaded().hasChanges).isTrue()

    viewModel.moveCategory(FOOD.id, USUAL.id, index = 0)
    assertThat(viewModel.loaded().hasChanges).isFalse()
  }

  @Test
  fun `Expense categories can't move into income groups`() = runReorderTest { viewModel ->
    viewModel.moveCategory(FOOD.id, INCOME.id, index = 0)
    viewModel.moveCategory(SALARY.id, USUAL.id, index = 0)

    assertThat(viewModel.loaded().groups).isEqualTo(LOADED)
  }

  @Test
  fun `Groups move among the expense groups only`() = runReorderTest { viewModel ->
    viewModel.moveGroup(BILLS.id, index = 0)
    viewModel.moveGroup(INCOME.id, index = 0)

    assertThat(viewModel.loaded().groups.map { it.id })
      .containsExactly(BILLS.id, USUAL.id, INCOME.id)
  }

  @Test
  fun `Discarding restores the loaded order`() = runReorderTest { viewModel ->
    viewModel.moveGroup(BILLS.id, index = 0)
    viewModel.discard()

    assertThat(viewModel.state.value)
      .isEqualTo(ReorderCategoriesState.Loaded(LOADED, hasChanges = false, isSaving = false))
  }

  @Test
  fun `Saving writes the edited order`() = runReorderTest { viewModel ->
    viewModel.moveGroup(BILLS.id, index = 0)

    viewModel.events.test {
      viewModel.save()
      assertThatNextEmissionIsEqualTo(Saved)
    }

    assertThat(order.saved.single().map { it.id }).containsExactly(BILLS.id, USUAL.id, INCOME.id)
    assertThat(viewModel.loaded().hasChanges).isFalse()
  }

  @Test
  fun `Saving with no changes writes nothing`() = runReorderTest { viewModel ->
    viewModel.save()

    assertThat(order.saved).isEmpty()
  }

  @Test
  fun `A failed save keeps the changes`() = runReorderTest { viewModel ->
    order.failSave = true
    viewModel.moveGroup(BILLS.id, index = 0)

    viewModel.events.test {
      viewModel.save()
      assertThatNextEmissionIsEqualTo(SaveFailed)
    }

    assertThat(viewModel.loaded()).all {
      prop(ReorderCategoriesState.Loaded::hasChanges).isTrue()
      prop(ReorderCategoriesState.Loaded::isSaving).isFalse()
    }
  }

  @Test
  fun `A failed load shows a failure`() =
    runReorderTest(failLoad = true) { viewModel ->
      assertThat(viewModel.state.value).isInstanceOf<ReorderCategoriesState.Failed>()
    }

  private fun ReorderCategoriesViewModel.loaded() = state.value as Loaded

  private fun runReorderTest(
    failLoad: Boolean = false,
    action: suspend TestScope.(ReorderCategoriesViewModel) -> Unit,
  ) = runTest {
    order.failLoad = failLoad
    val viewModel = ReorderCategoriesViewModel(order, unconfinedViewModelContext)
    action(viewModel)
  }

  private class FakeCategoryOrder : CategoryOrder {
    var failLoad = false
    var failSave = false
    val saved = mutableListOf<List<OrderedGroup>>()

    override suspend fun load(): List<OrderedGroup> {
      check(!failLoad) { "Load failed" }
      return LOADED.map { group ->
        OrderedGroup(
          id = group.id,
          name = group.name,
          isIncome = group.isIncome,
          isHidden = group.isHidden,
          categories = group.categories.map { OrderedCategory(it.id, it.name, it.isHidden) },
        )
      }
    }

    override suspend fun save(groups: List<OrderedGroup>) {
      check(!failSave) { "Save failed" }
      saved += groups
    }
  }

  private companion object {
    val FOOD = ReorderCategory(CategoryId("food"), "Food", isHidden = false)
    val FUN = ReorderCategory(CategoryId("fun"), "Fun", isHidden = false)
    val RENT = ReorderCategory(CategoryId("rent"), "Rent", isHidden = false)
    val SALARY = ReorderCategory(CategoryId("salary"), "Salary", isHidden = false)

    val USUAL = group("usual", "Usual", FOOD, FUN)
    val BILLS = group("bills", "Bills", RENT)
    val INCOME = group("income", "Income", SALARY, isIncome = true)
    val LOADED = persistentListOf(USUAL, BILLS, INCOME)

    fun group(
      id: String,
      name: String,
      vararg categories: ReorderCategory,
      isIncome: Boolean = false,
    ) =
      ReorderGroup(
        id = CategoryGroupId(id),
        name = name,
        isIncome = isIncome,
        isHidden = false,
        categories = persistentListOf(*categories),
      )
  }
}
