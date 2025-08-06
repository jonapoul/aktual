package actual.core.ui

import actual.di.core.ViewModelAssistedFactory
import actual.di.core.ViewModelGraphProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.reflect.KClass
import kotlin.reflect.cast

val LocalViewModelGraphProvider = staticCompositionLocalOf<ViewModelGraphProvider> {
  error("No ViewModelGraphProvider registered")
}

// Used to fetch ViewModel instances from ViewModelGraph
@Composable
inline fun <reified VM : ViewModel> viewModel(
  owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner found" },
  key: String? = null,
): VM = viewModel(
  viewModelStoreOwner = owner,
  key = key,
  factory = LocalViewModelGraphProvider.current,
)

/**
 * Used to fetch assisted ViewModel factory instances from ViewModelGraph. Note that there's no
 * compile-time validation that [VM] and [VMAF] types match up to each other (yet?)
 */
@Composable
inline fun <reified VM : ViewModel, reified VMAF : ViewModelAssistedFactory> assistedViewModel(
  owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner found" },
  key: String? = null,
  crossinline buildViewModel: VMAF.() -> VM,
): VM {
  val graphProvider = LocalViewModelGraphProvider.current
  return viewModel(
    viewModelStoreOwner = owner,
    key = key,
    factory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val nullableProvider = graphProvider
          .buildViewModelGraph(extras)
          .assistedFactoryProviders[VMAF::class]
          ?.invoke()
          ?.let(VMAF::class::cast)

        val factoryProvider = requireNotNull(nullableProvider) {
          "No factory found for class ${VMAF::class.qualifiedName}"
        }

        return modelClass.cast(factoryProvider.buildViewModel())
      }
    },
  )
}
