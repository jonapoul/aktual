---
name: dependency-browser
description: Extracts a Maven dependency's JAR (or sources JAR) into the project build dir so you can browse its classes and source. Use when you need to explore the API or internals of a library. Pass coordinates as `group:artifact:version` or `group:artifact:version:sources`. Omit version to have it looked up from gradle/libs.versions.toml.
tools: Bash, Read
model: haiku
---

# Dependency Browser

Resolve a Maven dependency from the local Gradle cache and extract it into
`build/dependency-browser/<group>/<artifact>/<version>/` so the caller can browse
its contents without leaving the project directory.

## Input

Expect coordinates in one of these forms:
- `group:artifact:version` — extract the main JAR (classes + resources)
- `group:artifact:version:sources` — extract the sources JAR instead
- `group:artifact` — look up the version from `gradle/libs.versions.toml` first
- `group:artifact:sources` — look up the version, then extract the sources JAR

## Step 0 — Resolve version if omitted

If the version field is missing or the classifier is `sources` in the third position,
look it up from `gradle/libs.versions.toml` before proceeding.

Search the toml for the group+artifact pair in the `[libraries]` section:

```bash
grep -n '<group>\|<artifact>' gradle/libs.versions.toml
```

The entry will look like:
```toml
some-alias = { module = "group:artifact", version = "1.2.3" }
# or with a version ref:
some-alias = { module = "group:artifact", version.ref = "some-version" }
```

If it uses `version.ref`, look up the ref in the `[versions]` section:
```bash
grep 'some-version' gradle/libs.versions.toml
```

Use the resolved version string. If not found in the toml, report clearly and stop.

## Extraction target

Always extract into the project's build directory:

```
build/dependency-browser/<group>/<artifact>/<version>/
```

or for sources:

```
build/dependency-browser/<group>/<artifact>/<version>-sources/
```

## Steps

### 1 — Check if already extracted

```bash
ls build/dependency-browser/<group>/<artifact>/<version>[-sources]/ 2>/dev/null
```

If files are listed, report the extraction dir and **stop** — do not re-extract.

### 2 — Locate the JAR in the Gradle cache

The local Gradle cache lives at `~/.gradle/caches/modules-2/files-2.1/`. Search it:

```bash
# Main JAR
find ~/.gradle/caches/modules-2/files-2.1/<group>/<artifact>/<version> \
     -name "<artifact>-<version>.jar" ! -name "*sources*" ! -name "*javadoc*" \
     2>/dev/null

# Sources JAR
find ~/.gradle/caches/modules-2/files-2.1/<group>/<artifact>/<version> \
     -name "<artifact>-<version>-sources.jar" \
     2>/dev/null
```

If no JAR is found, also check the Gradle modules cache at:
`~/.gradle/caches/modules-*/files-*/`

If still not found, use Gradle to fetch and resolve it first:

```bash
./gradlew --quiet --no-configuration-cache \
  -Pdep.group=<group> -Pdep.artifact=<artifact> -Pdep.version=<version> \
  dependencies 2>&1 | grep -i "could not\|error"
```

Then advise the caller to ensure the dependency appears in any configuration in
`build.gradle.kts` so Gradle resolves it.

### 3 — Create the target directory and extract

```bash
mkdir -p build/dependency-browser/<group>/<artifact>/<version>[-sources]

jar xf /path/to/found.jar \
    --dir=build/dependency-browser/<group>/<artifact>/<version>[-sources]/
```

(`jar xf --dir=` requires JDK 9+; fall back to `cd <target> && jar xf <absolute-path>` if needed.)

### 4 — Report

Print a short summary:

```
Extracted to: build/dependency-browser/<group>/<artifact>/<version>/
Top-level entries:
<list first 20 lines of extracted dir tree>
```

Use:
```bash
find build/dependency-browser/<group>/<artifact>/<version>[-sources]/ -maxdepth 3
```

## Rules

1. **Never extract to `/tmp`** — always use `build/dependency-browser/` under the project root
2. **Skip re-extraction** if the target directory already exists and is non-empty
3. Use **version-specific directories** — never mix versions of the same artifact
4. Do **not** modify any source files in the project
5. Do **not** run full Gradle builds — only `find` / `jar` / `mkdir` commands are needed in the happy path
6. Report the extraction path clearly so the caller knows where to browse
