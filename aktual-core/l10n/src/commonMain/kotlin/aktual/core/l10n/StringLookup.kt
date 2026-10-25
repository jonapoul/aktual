package aktual.core.l10n

import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

fun interface StringLookup {
  suspend operator fun get(resource: StringResource): String
}

@ContributesBinding(AppScope::class)
class StringLookupImpl : StringLookup {
  override suspend fun get(resource: StringResource): String = getString(resource)
}
