---
name: gradle-runner
description: Runs Gradle commands with filtered output to keep the parent context window clean. Use for any `./gradlew` or `gradle` invocation - compilation, tests, formatting checks.
tools: Bash
model: haiku
---

# Gradle Runner

Run a single Gradle command and return only the actionable output (errors, failures, warnings). This keeps
the parent context window clean by absorbing verbose Gradle noise in an isolated agent.

## Execution

Parse the caller's prompt for the Gradle task(s) to run and any extra flags, then run via the wrapper
script which handles output capture, filtering, and exit-code propagation:

```bash
./scripts/gradle-run.sh <tasks> [flags]
```

### Common invocations

```bash
# Compile one module (most common)
./scripts/gradle-run.sh :<module>:compileAll

# Run tests for one module - `:<module>:test` is AMBIGUOUS in this project; use a
# concrete task: `testAll`, `testAndroidHostTest`, or `testAndroid`
./scripts/gradle-run.sh :<module>:testAndroidHostTest --continue

# Format check
./scripts/ktfmt.sh check 2>&1 | grep -v "^$" | head -50
```

### Getting more output

The script prints a filtered view, capped at 200 lines, and ends with the path of the full log, e.g.
`Full log (1234 lines): /.../build/gradle-run/20261006-191500.log`. If the filtered view is missing
something the caller asked for, or says `TRUNCATED`, search that log instead of running Gradle again:

```bash
grep -nE "<pattern>" <log path> | head -100
sed -n '<from>,<to>p' <log path>
```

Test failure details are in `<module-dir>/build/test-results/**/*.xml` and detekt findings in
`<module-dir>/build/reports/detekt/issues.txt`.

### Timeout

Always pass **timeout: 600000** (10 minutes) to the Bash tool for all Gradle commands.

## Rules

1. Run Gradle commands **sequentially**, one Bash call at a time - never in parallel
2. **Report every command the caller asked for, separately and explicitly** - state each
   one's outcome (succeeded / failed) even when an earlier or later command fails. Never
   collapse the report down to just the failing command; a silent omission reads to the
   caller as "didn't run"
3. Report filtered output verbatim to the caller
4. If a task name is **ambiguous** (Gradle replies "task '...' is ambiguous ... Candidates
   are: ..."), report the failure AND list the candidate task names verbatim so the caller
   can pick one - do not guess and re-run
5. Do **not** attempt to fix issues - only report findings
6. Do **not** read or modify source files. Reading the full log and files under `build/` is fine
7. Do **not** run `./gradlew build` or `./gradlew allTests` - those are full-project builds and will peg the machine
8. **Never run the same Gradle command twice** to get more output - everything it printed is in
   the full log. If that log still doesn't have what the caller needs, say so in the report
