---
name: port-upstream-pr
description: Port a specific upstream Actual PR to Aktual - read the PR, decide whether it applies, implement it with matching tests, and open a draft PR (with before/after screenshots if UI changed). Backs out without changes if the PR isn't relevant or is already done. Use when given an actualbudget/actual PR number or URL to port.
argument-hint: "<upstream-pr-number-or-url>"
---

Port one upstream [actualbudget/actual](https://github.com/actualbudget/actual) PR to Aktual and open a draft PR for it.

Don't ask clarifying questions unless the backout decision is genuinely ambiguous (step 3). Otherwise execute.

## 1. Fetch the upstream PR

`$ARGUMENTS` is the PR number or URL. Then run:

```bash
.claude/skills/port-upstream-pr/fetch.sh <number-or-url>
```

It keeps a blobless clone of upstream in `build/upstream-actual`, and prints the title, state, release note (category and summary) and changed files, then writes the full diff to `build/upstream-actual/pr-<n>.diff`. Read the diff. Read whole upstream files where the diff needs context: `git -C build/upstream-actual show pr-<n>:<path>` (after) or `pr-<n>-base:<path>` (before).

In cloud sessions `gh` can't reach upstream, so the script falls back to git for the state and title. If you need the PR description or review discussion, use `add_repo` (owner `actualbudget`, repo `actual`, read access) and then the GitHub MCP `pull_request_read`. Only do this if the diff and release note don't explain the change.

If the PR isn't merged, say so in the final report and the PR body. Port it anyway, since the caller picked it.

## 2. Map it onto Aktual

Map each upstream change to its Aktual location:

| Upstream | Aktual |
| --- | --- |
| `packages/loot-core/src/server/**` | `aktual-budget/*/impl`, `aktual-budget/data/*`, domain modules |
| `packages/loot-core/src/shared/**` | domain modules, `aktual-budget/model`, `aktual-core:*` |
| `packages/loot-core/src/types/models/**` | `aktual-budget/model` |
| `packages/loot-core/migrations/**` | use the `apply-upstream-migration` skill for that part |
| `packages/desktop-client/src/components/mobile/**` | `ui` / `vm` modules for the matching screen |
| `packages/desktop-client/src/components/**` (desktop) | same, but only the behaviour. Aktual's UI is the mobile layout |
| `packages/desktop-client/src/locale/**`, `**/i18n` | `aktual-core:l10n` |
| `packages/sync-server/**` | `aktual-api` clients, only if the request/response shape changed |
| `packages/crdt/**` | `aktual-budget/sync` and related |

Where upstream changes both the desktop and mobile (narrow) variant of a component, follow the mobile one. Use `Grep` on Aktual for the names of upstream functions, components, types, SQL tables and strings to find the counterpart. Existing comments that say "upstream" often name the file they came from.

## 3. Decide whether to back out

Stop here if any of these hold:

- **Not relevant**: the PR only touches things Aktual doesn't have or doesn't mirror - docs, CI, upstream build tooling, Electron, the web-only service worker, server admin or hosting, the desktop-only layout with no behaviour change, dependency bumps, `upcoming-release-notes` alone, translations Aktual doesn't carry.
- **Missing prerequisite**: it changes a feature or screen Aktual hasn't implemented at all. Porting it means building that feature first, which is out of scope.
- **Already done**: Aktual already behaves the way the PR makes upstream behave. Check the code, not just the names: read the Aktual counterpart and compare it with the upstream "after" version. Also check `git log --grep='actual#<n>'` and `git log --grep='upstream.*<n>'`.

If only part of the PR applies, or part is already done, port the rest and list what you skipped and why in the PR body. Don't back out.

To back out: make no commits, create no branch or PR, delete anything you created, and leave the working tree as you found it. Report:

```
Backed out of actualbudget/actual#<n> (<title>): <Not relevant | Missing prerequisite | Already done>
<one or two lines of evidence: what the PR does and where Aktual stands, with file:line refs>
```

Ask the user only when it's a genuine judgement call, e.g. a desktop-only UI change that could reasonably be adapted to mobile.

## 4. Implement

Create a branch off `origin/main` named `upstream/pr-<n>`. In a cloud session with an assigned development branch, use that branch instead.

Follow the root `CLAUDE.md` and the module `CLAUDE.md`s as usual. Don't port line by line. Write idiomatic Kotlin in Aktual's architecture (domain / vm / ui, Metro, Molecule), keeping upstream's behaviour and edge cases.

Wherever logic is adapted from upstream (algorithms, conditions, edge cases, constants, SQL, query shapes), add a comment pointing to the source:

```kotlin
// Ported from upstream's packages/loot-core/src/shared/schedules.ts (actualbudget/actual#<n>)
```

Use the file the logic lives in at the PR head. One comment per ported function or block is enough. Don't annotate trivial UI wiring. Wrap at 100 columns.

New user-facing strings go in `aktual-core:l10n`. Reuse upstream's English wording where it fits.

## 5. Tests

Aktual's tests must cover at least what upstream's do:

1. List every test upstream adds or changes in the PR (`*.test.ts`, `*.test.tsx`, `*.spec.ts`, e2e tests in `packages/desktop-client/e2e`). For each one, write an equivalent Aktual test, in `commonTest` unless it needs Android (see root `CLAUDE.md`). Keep the cases and expected values, so a reviewer can check them against upstream. Add a source comment as in step 4 to the test class or to each ported case.
2. If upstream changed logic without changing tests, still test the Aktual change: VM tests with Turbine, domain/unit tests for logic, query tests for DB changes.
3. Upstream e2e or visual tests become VM or Compose UI tests here. Don't port Playwright snapshots.

Keep a table of upstream test → Aktual test for the PR body.

## 6. Validate

Use the `gradle-runner` agent for Gradle:

```bash
./scripts/compile.sh
./scripts/test.sh          # or a narrower task picked with the run-module-tests skill
./scripts/ktfmt.sh
./scripts/detekt.sh
```

Also run `./scripts/unusedStrings.sh` if strings were added or removed, and the `redundant-module-links` skill if module dependencies changed. Fix everything before going on. Don't run `./gradlew build` or `allTests`.

Commit with a short message, e.g. `Port actualbudget/actual#<n>: <what changed>`.

## 7. Screenshots (UI changes only)

Do this if the diff touches any composable, theme token or user-visible string (`*/ui/**`, `*/components/**`, `aktual-core/ui`, `aktual-core/l10n`). Follow the `pr-screenshots` skill, including its **Before/after** section, for every screen or state the change affects:

- Before: rendered from `origin/main`. After: rendered from your branch.
- If the screen or state doesn't exist on `origin/main` (e.g. a new dialog), use the nearest existing state as "before", or put `-` in the before cell.
- Look at every image before uploading.

Put them in the PR body as one side-by-side table:

```markdown
## Screenshots

| | Before | After |
| --- | --- | --- |
| Schedule list | ![](before-url) | ![](after-url) |
| Edit schedule | ![](before-url) | ![](after-url) |
```

## 8. Open a draft PR

Push the branch and open a **draft** PR against `main` on `jonapoul/aktual`. Use `gh pr create --draft --body-file <file>` if `gh` works, otherwise the GitHub MCP `create_pull_request` with `draft: true`.

Title: `Port actualbudget/actual#<n>: <upstream title>`

Body (keep the repo template's `Closes #` line only if there's an Aktual issue for it):

```markdown
Ports https://github.com/actualbudget/actual/pull/<n> (<merged | not merged yet>).

## Changes
- <what changed in Aktual, by module>

## Skipped
- <upstream parts not ported, and why. Omit this section if nothing was skipped>

## Tests
| Upstream | Aktual |
| --- | --- |
| `<file>` - `<test name>` | `<Kotlin test class>.<test>` |
| (none - logic change only) | `<Kotlin test class>.<test>` |

## Screenshots
<table from step 7, UI changes only>
```

Report the PR URL when done.
