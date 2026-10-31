---
name: pr-screenshots
description: Render changed Compose screens or components to PNG headlessly and attach them to a PR description. Use when asked for screenshots, UI images or previews on a PR, especially in Claude Code on the web where there's no display.
argument-hint: "[what to capture]"
---

Grab images of changed UI and put them in the PR description. Works with no display, so it suits cloud sessions. It renders composables in a desktop Compose test, not the running app.

## 1. Pick what to capture

- `$ARGUMENTS` says what to capture, if given. Otherwise take the composables changed on this branch (`git diff origin/main --stat`, then look in `*/ui/` and `*/components/` modules).
- Prefer the scaffold/content composable that takes plain state (e.g. `InfoScaffold(state, onAction)`), not the `Screen` that needs a VM. Build the state from the file's `@Preview` functions, which are private, so copy their arguments.
- Default to dark colors. Add `LightColors` (and `MidnightColors`) only when the change is about color, or when asked.
- Keep it small: one or two images per changed screen or state.

## 2. Write a throwaway test

Create `<module>/src/desktopTest/kotlin/<package>/ScreenshotTest.kt` in the module that owns the composable, in the same package so `internal` composables are visible. Every compose module already has `aktual-test:compose` on its test classpath.

```kotlin
package aktual.about.ui.info

import aktual.core.theme.LightColors
import aktual.test.screenshot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.test.Test

class ScreenshotTest {
  // Full screen, phone size, dark colors
  @Test fun info() = screenshot("info") { InfoScaffold(buildState = STATE, onAction = {}) }

  // Cropped to the composable's own size, for components and dialogs
  @Test
  fun dialog() = screenshot("no-update-dialog", fitContent = true) {
    NoUpdateFoundDialogContent(onDismiss = {})
  }

  // Interact before capture
  @Test
  fun sortSheet() =
    screenshot("sort-sheet", colors = LightColors, interact = { onNodeWithText("Sort").performClick() }) {
      LicensesScaffold(...)
    }
}
```

`screenshot()` lives in `aktual-test/compose/src/desktopMain/kotlin/aktual/test/Screenshots.kt`. Parameters: `colors`, `widthDp`/`heightDp` (default phone, use `MY_MONITOR_WIDTH_DP`/`MY_MONITOR_HEIGHT_DP` for desktop layouts), `density` (default 2), `fitContent`, `interact`.

Run it through the `gradle-runner` agent:

```bash
./gradlew :<module>:desktopTest --tests '<package>.ScreenshotTest'
```

Images land in `<module>/build/screenshots/<name>.png`.

## 3. Check every image

Open each PNG with the Read tool and look at it. Fix and rerun if it's blank, cut off, still showing a loading shimmer, or missing the change. Don't upload something you haven't looked at.

## 4. Delete the test

Delete `ScreenshotTest.kt`. It must never be committed. Copy the PNGs you want to keep somewhere outside the repo first if you're about to run a clean build.

## 5. Upload and add to the PR

Make sure your real changes are committed and the working tree is clean, then:

```bash
.claude/skills/pr-screenshots/upload.sh <module>/build/screenshots/*.png
```

It commits the images under `.github/pr-screenshots/`, commits their removal straight after, pushes both, and prints a markdown image line per file. The links point at the first commit's SHA, so they keep working after the files are gone and nothing lands on main after the squash merge.

Put the printed lines in the PR body under a `## Screenshots` heading. Use a table for before/after or several color schemes side by side:

```markdown
| Dark | Light |
| --- | --- |
| ![](url1) | ![](url2) |
```

- New PR: include them in the body when you create it (`gh pr create --body-file`).
- Existing PR: `gh pr view --json body -q .body > body.md`, add the section, `gh pr edit --body-file body.md`.
- If `gh` isn't installed (some cloud sessions), use the GitHub MCP tools to create or update the PR instead.

## Before/after

Only when asked, or when a visual tweak is hard to judge without a comparison. Commit your changes, then `git switch --detach origin/main`. The untracked test file comes along. Adjust it if it uses anything new to this branch, run it, copy the PNGs out of `build/` as `<name>-before.png`, then `git switch -` and capture the after images.
