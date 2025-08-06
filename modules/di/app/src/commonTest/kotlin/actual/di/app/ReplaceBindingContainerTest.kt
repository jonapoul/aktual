@file:Suppress("FunctionOnlyReturningConstant")

package actual.di.app

import actual.core.model.UuidGenerator
import alakazam.kotlin.core.InfiniteLoopController
import alakazam.kotlin.core.LoopController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraph
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

@DependencyGraph(
  scope = AppScope::class,
  excludes = [UuidGeneratorContainer::class],
)
internal interface ReplaceBindingContainerTestGraph {
  // default
  val loopController: LoopController

  // replaced
  val uuidGenerator: UuidGenerator
}

private val testUuid = Uuid.parse("d0dcae38-ed41-4512-817c-e97e0f7c1f0e")

@ContributesTo(AppScope::class)
@BindingContainer
internal object TestUuidGeneratorContainer {
  @Provides
  fun generator(): UuidGenerator = UuidGenerator { testUuid }
}

class ReplaceBindingContainerTest {
  @Test
  fun `Test replacing containers in graph`() = runTest {
    val graph = createGraph<ReplaceBindingContainerTestGraph>()

    // default
    assertEquals(expected = InfiniteLoopController, actual = graph.loopController)

    // replaced
    assertEquals(expected = testUuid, actual = graph.uuidGenerator())
  }
}
