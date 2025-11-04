/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.test

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.text.AnnotatedString

fun SemanticsNodeInteraction.assertEditableTextEquals(expected: AnnotatedString): SemanticsNodeInteraction =
  assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText, expected))

fun SemanticsNodeInteraction.assertEditableTextEquals(expected: String): SemanticsNodeInteraction =
  assertEditableTextEquals(AnnotatedString(expected))

fun SemanticsNodeInteraction.assertEditableTextEquals(expected: Any): SemanticsNodeInteraction =
  assertEditableTextEquals(expected.toString())
