package actual.core.files

import java.io.File

interface FileSystem {
  fun budgetFile(budgetId: String): File
}
