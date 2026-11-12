package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.FontSizeMode
import aktual.budget.rules.components.SectionLabel
import aktual.budget.rules.components.SettingRow
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.disambiguated
import aktual.core.ui.switch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp

// Rows of the formula card's settings

@Composable
internal fun Section(label: String, content: @Composable ColumnScope.() -> Unit) = Column {
  AnimatedContent(targetState = label, transitionSpec = { fadeIn() togetherWith fadeOut() }) { text
    ->
    SectionLabel(text)
  }
  Column(
    modifier = Modifier.fillMaxWidth().clip(CardShape).background(colors.tableBackground),
    content = content,
  )
}

@Composable
internal fun ValueRow(
  label: String,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  showChevron: Boolean = onClick != null,
  value: @Composable RowScope.() -> Unit,
) =
  SettingRow(modifier = modifier, onClick = onClick, showChevron = showChevron) {
    Text(text = label, style = typography.bodyLarge, color = colors.tableText)
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
      verticalAlignment = CenterVertically,
      content = value,
    )
  }

@Composable
internal fun SettingValueRow(label: String, value: String, onClick: () -> Unit) =
  ValueRow(modifier = Modifier.testTag(Tags.CustomSetting), label = label, onClick = onClick) {
    Value(text = value)
  }

@Composable
internal fun SwitchRow(
  label: String,
  isChecked: Boolean,
  isEnabled: Boolean = true,
  onChange: (Boolean) -> Unit,
) =
  SettingRow(
    modifier = Modifier.testTag(Tags.CustomSwitch),
    onClick = { onChange(!isChecked) }.takeIf { isEnabled },
    showChevron = false,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = label,
      style = typography.bodyLarge,
      color = if (isEnabled) colors.tableText else colors.tableHeaderText,
    )
    Switch(
      checked = isChecked,
      onCheckedChange = onChange,
      enabled = isEnabled,
      colors = colors.switch(),
    )
  }

@Composable
internal fun Value(text: String, isCode: Boolean = false) =
  Text(
    text = text,
    style = if (isCode) typography.bodySmall.disambiguated() else typography.bodyMedium,
    color = colors.tableHeaderText,
    maxLines = 1,
    overflow = Ellipsis,
  )

@Composable
internal fun fontSizeString(mode: FontSizeMode, size: Double): String {
  if (mode != Static) return Strings.reportsFormulaEditorFontDynamic
  val text = if (size % 1.0 == 0.0) size.toLong().toString() else size.toString()
  return Strings.reportsFormulaViewerStaticSize(text)
}
