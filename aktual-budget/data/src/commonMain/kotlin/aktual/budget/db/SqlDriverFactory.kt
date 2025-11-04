/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.db

import app.cash.sqldelight.db.SqlDriver

fun interface SqlDriverFactory {
  fun create(): SqlDriver
}
