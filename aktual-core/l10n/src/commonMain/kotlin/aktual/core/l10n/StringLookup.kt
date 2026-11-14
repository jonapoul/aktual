package aktual.core.l10n

import org.jetbrains.compose.resources.StringResource

fun interface StringLookup {
  suspend operator fun get(resource: StringResource): String
}
