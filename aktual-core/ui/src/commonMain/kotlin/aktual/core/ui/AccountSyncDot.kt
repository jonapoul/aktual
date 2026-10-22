package aktual.core.ui

import aktual.budget.model.AccountSyncState
import aktual.budget.model.AccountSyncState.Failed
import aktual.budget.model.AccountSyncState.Ok
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private val DotSize = 8.dp

// Draws nothing for an account that isn't linked to a bank
@Composable
fun AccountSyncDot(state: AccountSyncState, modifier: Modifier = Modifier) {
  val (color, description) =
    when (state) {
      NotLinked -> return
      is Ok -> colors.sidebarItemBackgroundPositive to Strings.bankSyncIndicatorOk
      is Failed -> colors.sidebarItemBackgroundFailed to Strings.bankSyncIndicatorFailed
    }

  Box(
    modifier =
      modifier.size(DotSize).background(color, CircleShape).semantics {
        contentDescription = description
      },
  )
}
