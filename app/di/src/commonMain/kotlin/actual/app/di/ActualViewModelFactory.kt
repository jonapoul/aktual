package actual.app.di

import actual.core.di.ViewModelGraphProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface ActualViewModelFactory : ViewModelGraphProvider {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
    buildViewModelGraph(extras)
      .viewModelProviders[modelClass]
      ?.invoke()
      ?.let(modelClass::cast)
      ?: error("Unknown view model class $modelClass")
}
