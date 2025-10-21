/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

typealias ComposableLambda = @Composable () -> Unit

@Stable
fun ComposableLambda(block: @Composable () -> Unit): ComposableLambda = block
