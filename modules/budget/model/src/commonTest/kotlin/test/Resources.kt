/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package test

import java.io.File

internal fun getResource(name: String) =
  requireNotNull(System.getProperty("test.resourcesDir"))
    .let(::File)
    .resolve(name)
    .readText()
