package aktual.app.di

import aktual.core.l10n.StringLookup
import aktual.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

@ContributesBinding(AppScope::class)
class StringLookupImpl : StringLookup {
  override suspend fun get(resource: StringResource): String = getString(resource)
}
