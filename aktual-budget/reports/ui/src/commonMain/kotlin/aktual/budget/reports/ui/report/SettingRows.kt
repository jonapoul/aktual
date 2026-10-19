package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.vm.FontSizeMode
import aktual.core.icons.material.ChevronRight
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.switch
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily.Companion.Monospace
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

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
internal fun SectionLabel(text: String) =
  Text(
    modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
    text = text.uppercase(),
    style = typography.labelMedium.copy(fontWeight = SemiBold, letterSpacing = 0.08.em),
    color = colors.tableHeaderText,
  )

// The chevron slides in and out with [showChevron], so rows can change between viewing and editing
@Composable
internal fun SettingRow(
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  verticalAlignment: Alignment.Vertical = CenterVertically,
  showChevron: Boolean = onClick != null,
  content: @Composable RowScope.() -> Unit,
) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
        .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp),
    verticalAlignment = verticalAlignment,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = verticalAlignment,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      content = content,
    )
    AnimatedVisibility(
      visible = showChevron,
      enter = fadeIn() + expandHorizontally(),
      exit = fadeOut() + shrinkHorizontally(),
    ) {
      Icon(
        modifier = Modifier.padding(start = 12.dp).size(CHEVRON_SIZE),
        imageVector = MaterialIcons.ChevronRight,
        contentDescription = null,
        tint = colors.tableHeaderText,
      )
    }
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
internal fun Value(text: String, monospace: Boolean = false) =
  Text(
    text = text,
    style =
      if (monospace) typography.bodySmall.copy(fontFamily = Monospace) else typography.bodyMedium,
    color = colors.tableHeaderText,
    maxLines = 1,
    overflow = Ellipsis,
  )

@Composable
internal fun Divider() =
  Box(
    modifier =
      Modifier.padding(horizontal = 16.dp)
        .fillMaxWidth()
        .height(1.dp)
        .background(colors.tableBorder),
  )

@Composable
internal fun fontSizeString(mode: FontSizeMode, size: Double): String {
  if (mode != Static) return Strings.reportsFormulaEditorFontDynamic
  val text = if (size % 1.0 == 0.0) size.toLong().toString() else size.toString()
  return Strings.reportsFormulaViewerStaticSize(text)
}

private val CHEVRON_SIZE = 20.dp
