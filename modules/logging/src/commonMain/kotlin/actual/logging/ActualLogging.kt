package actual.logging

import alakazam.kotlin.logging.Logger

object ActualLogging {
  fun init(factory: TreeFactory, storage: LogStorage) {
    with(factory) {
      Logger.plant(systemLogging())
      Logger.plant(fileLogging(storage))
    }
  }
}
