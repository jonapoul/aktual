package actual.logging

import alakazam.kotlin.logging.Tree

interface TreeFactory {
  fun systemLogging(): Tree
  fun fileLogging(storage: LogStorage): Tree
}
