package aktual.budget.reports.vm.choosetype

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ChooseReportTypeDialog {
  data object UnsupportedType : ChooseReportTypeDialog
}
