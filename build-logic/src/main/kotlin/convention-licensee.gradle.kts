import app.cash.licensee.UnusedAction

plugins {
  id("app.cash.licensee")
}

licensee {
  listOf(
    "Apache-2.0",
    "MIT",
    "EPL-1.0",
    "BSD-2-Clause",
  ).forEach { spdxId ->
    allow(spdxId)
  }

  unusedAction(UnusedAction.IGNORE)
}
