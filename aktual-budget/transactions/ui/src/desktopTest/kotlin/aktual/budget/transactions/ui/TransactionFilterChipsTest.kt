package aktual.budget.transactions.ui

import aktual.budget.model.ConditionSet
import aktual.budget.model.TransactionFilterId
import aktual.budget.rules.components.ConditionTags
import aktual.budget.rules.domain.ConditionEntityLists
import aktual.budget.rules.domain.newCondition
import aktual.budget.transactions.domain.SavedFilter
import aktual.budget.transactions.vm.SavedFiltersState
import aktual.test.setPreviewContent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

class TransactionFilterChipsTest {
  @Test
  fun `Adding a filter picks a field, then adds the condition on Done`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent {
      TransactionFilterChips(
        conditions = ConditionSet(emptyList()),
        entities = ENTITIES,
        saved = SavedFiltersState(),
        onAction = { actions += it },
      )
    }

    // when
    onNodeWithText("Add filter").performClick()
    onNodeWithText("Cleared").performClick()
    onNodeWithTag(ConditionTags.QuerySheetDone).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()

    // then
    assertThat(actions).containsExactly(Action.AddCondition(newCondition(Cleared)))
  }

  @Test
  fun `Unsaved conditions are saved under a new name`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    setPreviewContent {
      TransactionFilterChips(
        conditions = ConditionSet(listOf(newCondition(Cleared))),
        entities = ENTITIES,
        saved = SavedFiltersState(),
        onAction = { actions += it },
      )
    }

    // when
    onNodeWithText("Unsaved filter").performClick()
    onNodeWithText("Save new filter").performClick()
    waitForIdle()
    onNode(hasSetTextAction()).performTextInput("Groceries")
    onNodeWithText("Save").performClick()

    // then
    assertThat(actions).containsExactly(Action.SaveFilter("Groceries"))
  }

  @Test
  fun `A modified saved filter can be updated`() = runComposeUiTest {
    // given
    val actions = mutableListOf<Action>()
    val filter =
      SavedFilter(TransactionFilterId("f"), "Groceries", ConditionSet(listOf(newCondition(Payee))))
    setPreviewContent {
      TransactionFilterChips(
        conditions = ConditionSet(listOf(newCondition(Cleared))),
        entities = ENTITIES,
        saved = SavedFiltersState(persistentListOf(filter), applied = filter, isModified = true),
        onAction = { actions += it },
      )
    }

    // when
    onNodeWithText("Groceries (modified)").performClick()
    onNodeWithText("Update filter").performClick()
    waitForIdle()

    // then
    assertThat(actions).containsExactly(Action.UpdateSavedFilter)
  }

  private companion object {
    val ENTITIES =
      ConditionEntityLists(
        accounts = persistentListOf(),
        categories = persistentListOf(),
        categoryGroups = persistentListOf(),
        payees = persistentListOf(),
        names = persistentMapOf(),
      )
  }
}
