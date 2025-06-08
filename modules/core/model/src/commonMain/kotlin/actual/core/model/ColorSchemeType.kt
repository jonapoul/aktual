package actual.core.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ColorSchemeType {
  data object System : ColorSchemeType
  data object Light : ColorSchemeType
  data object Dark : DarkColorSchemeType
  data object Midnight : DarkColorSchemeType
}

@Immutable
sealed interface DarkColorSchemeType : ColorSchemeType
