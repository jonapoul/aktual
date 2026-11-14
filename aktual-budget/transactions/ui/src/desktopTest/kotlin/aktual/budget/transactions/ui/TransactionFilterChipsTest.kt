package aktual.budget.transactions.ui

import aktual.budget.model.ConditionSet
import aktual.budget.rules.components.ConditionTags
import aktual.budget.rules.domain.ConditionEntityLists
import aktual.budget.rules.domain.newFilterCondition
import aktual.test.setPreviewContent
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
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
        onAction = { actions += it },
      )
    }

    // when
    onNodeWithText("Add filter").performClick()
    onNodeWithText("Cleared").performClick()
    onNodeWithTag(ConditionTags.QuerySheetDone).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()

    // then
    assertThat(actions).containsExactly(Action.AddCondition(newFilterCondition(Cleared)))
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
