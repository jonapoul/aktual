package aktual.about.data

import aktual.core.model.Bytes
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable data class LogFile(val date: LocalDate, val size: Bytes, val errorCount: Int)
