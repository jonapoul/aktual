package actual.core.ui

import actual.core.di.ViewModelFactory
import actual.core.di.ViewModelProviders
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.reflect.cast

@Composable
inline fun <reified VM : ViewModel> metroViewModel(
  viewModelStoreOwner: ViewModelStoreOwner = requireViewModelStoreOwner(),
  key: String? = null,
): VM = viewModel(
  viewModelStoreOwner = viewModelStoreOwner,
  key = key,
  factory = LocalViewModelProvidersBuilder.current,
)

@Composable
inline fun <reified VM : ViewModel, reified F : ViewModelFactory> metroViewModel(
  viewModelStoreOwner: ViewModelStoreOwner = requireViewModelStoreOwner(),
  key: String? = null,
  crossinline factory: F.() -> VM,
): VM {
  val providersBuilder = LocalViewModelProvidersBuilder.current
  return viewModel(
    viewModelStoreOwner = viewModelStoreOwner,
    key = key,
    factory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val factoryProvider = requireNotNull(providersBuilder.build(extras).factoryProviders[F::class]) {
          "No factory found for class ${F::class.qualifiedName}"
        }
        val vmFactory = factoryProvider().let(F::class::cast)
        return modelClass.cast(factory(vmFactory))!!
      }
    },
  )
}

val LocalViewModelProvidersBuilder = staticCompositionLocalOf<ViewModelProviders.Builder> {
  error("No ViewModelProvider.Factory registered")
}

@Composable
fun requireViewModelStoreOwner() = checkNotNull(LocalViewModelStoreOwner.current) {
  "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
}
