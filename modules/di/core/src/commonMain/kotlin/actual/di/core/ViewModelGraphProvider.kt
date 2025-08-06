package actual.di.core

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

interface ViewModelGraphProvider : ViewModelProvider.Factory {
  fun buildViewModelGraph(extras: CreationExtras): ViewModelGraph
}
