package actual.logging

import alakazam.kotlin.logging.Tree
import kotlinx.datetime.Clock
import java.io.PrintStream

class JvmTreeFactory(
  private val stream: PrintStream = System.out,
  private val clock: Clock = Clock.System,
) : TreeFactory {
  override fun systemLogging(): Tree = TimestampedPrintStreamTree(stream, clock)
  override fun fileLogging(storage: LogStorage): Tree = JvmLogbackTree(storage)
}
