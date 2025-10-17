package actual.core.model

import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File

interface Files {
  fun userHome(): Path
}

object SystemFiles : Files {
  override fun userHome(): Path = System
    .getProperty("user.home")
    .let(::File)
    .toOkioPath()
    .resolve(".actual")
}
