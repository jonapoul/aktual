package actual.di.app

import actual.account.model.Password
import actual.core.model.ServerUrl
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides

@BindingContainer
class DebugContainer(
  defaultPassword: Password.Provider,
  defaultServerUrl: ServerUrl.Provider,
) {
  @Provides val defaultPassword: Password.Provider = defaultPassword
  @Provides val defaultServerUrl: ServerUrl.Provider = defaultServerUrl
}
