/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.test

import aktual.core.di.AssistedFactoryKey
import aktual.core.di.ViewModelAssistedFactory
import aktual.core.di.ViewModelKey
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides

/**
 * Needed in case we're testing a module with only one VM or one VMAF - in which case the other map in ViewModelGraph
 * will be empty and we'll need to populate it with a dummy value to avoid Metro compile-time failures.
 *
 * The containers aren't given a scope because we only want to apply it to test graphs as needed.
 */

@Inject
class DummyViewModel : ViewModel()

@BindingContainer
object DummyViewModelContainer {
  @Provides
  @IntoMap
  @ViewModelKey(DummyViewModel::class)
  fun dummy(): ViewModel = DummyViewModel()
}

@Inject
class DummyViewModelAssistedFactory : ViewModelAssistedFactory

@BindingContainer
object DummyViewModelAssistedFactoryContainer {
  @Provides
  @IntoMap
  @AssistedFactoryKey(DummyViewModelAssistedFactory::class)
  fun dummy(): ViewModelAssistedFactory = DummyViewModelAssistedFactory()
}
