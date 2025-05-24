package actual.logging

import alakazam.kotlin.logging.Tree

object AndroidTreeFactory : TreeFactory {
  override fun systemLogging(): Tree = AndroidLogcatTree()
  override fun fileLogging(storage: LogStorage): Tree = AndroidLogbackTree(storage)
}
