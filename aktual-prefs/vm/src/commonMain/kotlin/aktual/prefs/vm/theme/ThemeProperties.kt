package aktual.prefs.vm.theme

import aktual.core.theme.Colors
import aktual.core.theme.attributes
import aktual.prefs.vm.inspect.ThemeProperty
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun Colors.properties(): ImmutableList<ThemeProperty> =
  attributes().map { (name, color) -> ThemeProperty(name = name, color = color) }.toImmutableList()
