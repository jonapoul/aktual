package aktual.app.di

import aktual.api.client.AccountApi
import aktual.di.Accessor
import aktual.di.AccessorKey
import aktual.di.ServerChosenScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides

/** Used in ServerUrlViewModel */
@BindingContainer
@ContributesTo(ServerChosenScope::class)
object ServerChosenContainer {
  @Provides
  @IntoMap
  @AccessorKey(AccountApi::class)
  fun account(api: AccountApi): Accessor = { api }
}
