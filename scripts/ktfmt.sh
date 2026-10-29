#!/bin/sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT" || exit 1

# shellcheck source=lib/git.sh
. "$SCRIPT_DIR/lib/git.sh"

# Parse ktfmt version from gradle/libs.versions.toml
LIBS_TOML="$ROOT/gradle/libs.versions.toml"
if [ -f "$LIBS_TOML" ]; then
    KTFMT_VERSION=$(grep '^ktfmt = ' "$LIBS_TOML" | sed 's/.*version = "\([^"]*\)".*/\1/')
else
    echo "Error: gradle/libs.versions.toml not found"
    exit 1
fi

if [ -z "$KTFMT_VERSION" ]; then
    echo "Error: ktfmt version not found in $LIBS_TOML"
    exit 1
fi

# The JAR is named by version, so a version bump downloads a fresh one rather than reusing a
# stale cache
JAR_DIR="$ROOT/build/ktfmt"
KTFMT_JAR="$JAR_DIR/ktfmt-$KTFMT_VERSION.jar"

# Parse arguments
MODE="format"
FORCE=false
for arg in "$@"; do
    case "$arg" in
        format|check) MODE="$arg" ;;
        --force) FORCE=true ;;
        *)
            echo "Usage: $0 [format|check] [--force]"
            echo "  format  - Format Kotlin files (default)"
            echo "  check   - Check formatting without modifying files"
            echo "  --force - Format all Kotlin files, not just changed ones"
            exit 1
            ;;
    esac
done

# Determine ktfmt arguments based on mode
if [ "$MODE" = "check" ]; then
    KTFMT_ARGS="--google-style --dry-run --set-exit-if-changed"
else
    KTFMT_ARGS="--google-style"
fi

# Resolve ktfmt binary: system PATH if it's the expected version, otherwise the cached JAR
USE_SYSTEM_KTFMT=false
if command -v ktfmt >/dev/null 2>&1; then
    SYSTEM_VERSION=$(ktfmt --version 2>/dev/null | awk '{ print $NF }')
    if [ "$SYSTEM_VERSION" = "$KTFMT_VERSION" ]; then
        USE_SYSTEM_KTFMT=true
    else
        echo "System ktfmt version ($SYSTEM_VERSION) doesn't match $KTFMT_VERSION, using the JAR instead"
    fi
fi

if [ "$USE_SYSTEM_KTFMT" = false ] && [ ! -f "$KTFMT_JAR" ]; then
    echo "Downloading ktfmt $KTFMT_VERSION..."
    mkdir -p "$JAR_DIR"

    DOWNLOAD_URL="https://github.com/Kotlin/ktfmt/releases/download/v${KTFMT_VERSION}/ktfmt-${KTFMT_VERSION}-with-dependencies.jar"

    # Download to a temporary name so a failed or interrupted download never leaves a broken JAR
    # in the cache
    PARTIAL="$KTFMT_JAR.part"
    if command -v curl >/dev/null 2>&1; then
        curl -fL "$DOWNLOAD_URL" -o "$PARTIAL" && mv "$PARTIAL" "$KTFMT_JAR"
    elif command -v wget >/dev/null 2>&1; then
        wget "$DOWNLOAD_URL" -O "$PARTIAL" && mv "$PARTIAL" "$KTFMT_JAR"
    else
        echo "Neither curl nor wget found. Please install one to download ktfmt."
        exit 1
    fi
    if [ ! -f "$KTFMT_JAR" ]; then
        rm -f "$PARTIAL"
        echo "Failed to download ktfmt from $DOWNLOAD_URL"
        exit 1
    fi
    echo "ktfmt $KTFMT_VERSION downloaded to $KTFMT_JAR"

    # Clear out JARs for other versions, and the old unversioned layout
    for old in "$JAR_DIR"/ktfmt*.jar "$JAR_DIR/ktfmt.version"; do
        if [ "$old" != "$KTFMT_JAR" ]; then rm -f "$old"; fi
    done

    # Force formatting all files when a new binary is downloaded
    FORCE=true
fi

# Collect files to format. Only files git tracks or would track, so ignored directories (build
# output, gradle-profiler's Gradle home) are skipped.
if [ "$FORCE" = true ]; then
    echo "Formatting all Kotlin files (mode: $MODE)"
    KOTLIN_FILES=$(git -c core.quotePath=false ls-files -co --exclude-standard -- '*.kt' '*.kts' \
        | while IFS= read -r file; do if [ -f "$file" ]; then printf '%s\n' "$file"; fi; done)
else
    BASE_BRANCH=$(git_default_base)
    MERGE_BASE=$(git_merge_base "$BASE_BRANCH") || exit 1
    MERGE_BASE_SHORT=$(git_short_sha "$MERGE_BASE")
    # Shared change detection returns all files; ktfmt only formats Kotlin sources
    KOTLIN_FILES=$(git_changed_files "$MERGE_BASE" | grep -E '\.kts?$' || true)
    FILE_COUNT=$(echo "$KOTLIN_FILES" | grep -c . || true)

    if [ "$FILE_COUNT" -eq 0 ]; then
        echo "No Kotlin files changed since $BASE_BRANCH ($MERGE_BASE_SHORT), nothing to do."
        exit 0
    fi

    echo "$FILE_COUNT file(s) changed since $BASE_BRANCH ($MERGE_BASE_SHORT) (mode: $MODE)"
fi

list_files() {
    printf '%s\n' "$KOTLIN_FILES" | grep . | tr '\n' '\0'
}

run_ktfmt() {
    list_files | xargs -0 "$@"
}

checksum_files() {
    list_files | xargs -0 cksum | sort
}

# Resolve the java binary: prefer JAVA_HOME, fall back to PATH
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_BIN="$JAVA_HOME/bin/java"
else
    JAVA_BIN="java"
fi

if [ "$MODE" = "format" ]; then
    BEFORE=$(mktemp)
    AFTER=$(mktemp)
    trap 'rm -f "$BEFORE" "$AFTER"' EXIT
    checksum_files > "$BEFORE"
fi

# Run ktfmt
if [ "$USE_SYSTEM_KTFMT" = true ]; then
    echo "Using system ktfmt v$SYSTEM_VERSION (mode: $MODE)"
    # KTFMT_ARGS holds multiple space-separated flags that must word-split
    # shellcheck disable=SC2086
    run_ktfmt ktfmt $KTFMT_ARGS
else
    echo "Using cached ktfmt v$KTFMT_VERSION JAR (mode: $MODE)"
    # shellcheck disable=SC2086
    run_ktfmt "$JAVA_BIN" -jar "$KTFMT_JAR" $KTFMT_ARGS
fi
KTFMT_EXIT=$?

# xargs reports failures with its own codes (123, 124, ...), so collapse them to a plain failure
if [ "$KTFMT_EXIT" -ne 0 ]; then
    exit 1
fi

if [ "$MODE" = "format" ]; then
    checksum_files > "$AFTER"
    CHANGED_COUNT=$(comm -13 "$BEFORE" "$AFTER" | grep -c . || true)
    echo "$CHANGED_COUNT file(s) reformatted"
fi
exit 0
