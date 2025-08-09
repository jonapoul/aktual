package actual.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable

typealias ComposableLambda = @Composable () -> Unit

@Stable
fun ComposableLambda(block: @Composable () -> Unit): ComposableLambda = block
