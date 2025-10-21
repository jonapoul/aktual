/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import aktual.core.di.ViewModelAssistedFactory
import aktual.core.di.ViewModelGraphProvider
import aktual.core.di.assisted
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
inline fun <reified VM : ViewModel> metroViewModel(
  owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner found" },
  key: String? = null,
  graphProvider: ViewModelGraphProvider = LocalViewModelGraphProvider.current,
): VM = viewModel(
  viewModelStoreOwner = owner,
  key = key,
  factory = graphProvider,
)

/**
 * Used to fetch assisted ViewModel factory instances from ViewModelGraph. Note that there's no
 * compile-time validation that [VM] and [VMAF] types match up to each other (yet?)
 */
@Composable
inline fun <reified VM : ViewModel, reified VMAF : ViewModelAssistedFactory> assistedMetroViewModel(
  owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner found" },
  key: String? = null,
  graphProvider: ViewModelGraphProvider = LocalViewModelGraphProvider.current,
  crossinline buildViewModel: VMAF.() -> VM,
): VM {
  return viewModel(
    viewModelStoreOwner = owner,
    key = key,
    factory = object : ViewModelProvider.Factory {
      override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val vm = graphProvider
          .buildViewModelGraph(extras)
          .assisted(buildViewModel)
        return modelClass.cast(vm)
      }
    },
  )
}
