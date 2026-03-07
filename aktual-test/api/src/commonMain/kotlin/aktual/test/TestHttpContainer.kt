package aktual.test

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import io.ktor.client.engine.HttpClientEngine

@BindingContainer
class TestHttpContainer(private val engine: HttpClientEngine) {
  @Provides fun engine(): HttpClientEngine = engine
}
