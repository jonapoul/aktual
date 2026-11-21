package aktual.prefs.vm.theme

import aktual.prefs.vm.inspect.ThemeProperty
import aktual.theme.Colors
import aktual.theme.attributes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun Colors.properties(): ImmutableList<ThemeProperty> =
  attributes().map { (name, color) -> ThemeProperty(name = name, color = color) }.toImmutableList()
