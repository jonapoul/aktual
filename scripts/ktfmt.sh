#!/bin/sh

SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit

JAR_DIR="$SCRIPT_DIR/../build/ktfmt"
KTFMT_JAR="$JAR_DIR/ktfmt.jar"
KTFMT_VERSION_FILE="$JAR_DIR/ktfmt.version"

# Parse ktfmt version from gradle/libs.versions.toml
LIBS_TOML="$SCRIPT_DIR/../gradle/libs.versions.toml"
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

# Check if cached version differs from current version
if [ -f "$KTFMT_VERSION_FILE" ]; then
    CACHED_VERSION=$(cat "$KTFMT_VERSION_FILE")
    if [ "$CACHED_VERSION" != "$KTFMT_VERSION" ]; then
        echo "Version changed from $CACHED_VERSION to $KTFMT_VERSION, removing old JAR..."
        rm -f "$KTFMT_JAR" "$KTFMT_VERSION_FILE"
    fi
fi

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

# Collect files to format
if [ "$FORCE" = true ]; then
    echo "Formatting all Kotlin files (mode: $MODE)"
    run_ktfmt() {
        find . -type d -name build -prune -o -type f \( -name "*.kt" -o -name "*.kts" \) -print0 | xargs -0 "$@"
    }
else
    COMMIT_SHORT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    CHANGED_FILES=$(
        {
            git diff --name-only HEAD -- '*.kt' '*.kts' 2>/dev/null
            git diff --name-only --cached HEAD -- '*.kt' '*.kts' 2>/dev/null
            git ls-files --others --exclude-standard -- '*.kt' '*.kts' 2>/dev/null
        } | sort -u | while IFS= read -r file; do
            [ -f "$file" ] && printf '%s\n' "$file"
        done
    )
    FILE_COUNT=$(echo "$CHANGED_FILES" | grep -c . || true)

    if [ "$FILE_COUNT" -eq 0 ]; then
        echo "No Kotlin files changed since commit $COMMIT_SHORT, nothing to do."
        exit 0
    fi

    echo "$FILE_COUNT file(s) changed since commit $COMMIT_SHORT (mode: $MODE)"
    run_ktfmt() {
        echo "$CHANGED_FILES" | tr '\n' '\0' | xargs -0 "$@"
    }
fi

# Check if ktfmt is available in PATH
if command -v ktfmt >/dev/null 2>&1; then
    SYSTEM_VERSION=$(ktfmt --version 2>/dev/null | sed 's/ktfmt version //')

    if [ "$SYSTEM_VERSION" != "$KTFMT_VERSION" ]; then
        echo "⚠️  Warning: System ktfmt version ($SYSTEM_VERSION) does not match expected version ($KTFMT_VERSION)"
        echo "   Consider updating ktfmt or using the cached JAR version."
    fi

    echo "Using system ktfmt v$SYSTEM_VERSION (mode: $MODE)"
    run_ktfmt ktfmt $KTFMT_ARGS
    exit $?
fi

# If not in PATH, check for cached JAR
if [ ! -f "$KTFMT_JAR" ]; then
    echo "ktfmt not found in PATH, downloading version $KTFMT_VERSION..."
    mkdir -p "$JAR_DIR"

    DOWNLOAD_URL="https://github.com/facebook/ktfmt/releases/download/v${KTFMT_VERSION}/ktfmt-${KTFMT_VERSION}-with-dependencies.jar"

    if command -v curl >/dev/null 2>&1; then
        curl -L "$DOWNLOAD_URL" -o "$KTFMT_JAR" || { echo "Failed to download ktfmt"; exit 1; }
    elif command -v wget >/dev/null 2>&1; then
        wget "$DOWNLOAD_URL" -O "$KTFMT_JAR" || { echo "Failed to download ktfmt"; exit 1; }
    else
        echo "Neither curl nor wget found. Please install one to download ktfmt."
        exit 1
    fi

    echo "$KTFMT_VERSION" > "$KTFMT_VERSION_FILE"
    echo "ktfmt $KTFMT_VERSION downloaded to $KTFMT_JAR"
fi

# Run cached JAR
echo "Using cached ktfmt v$KTFMT_VERSION JAR (mode: $MODE)"
run_ktfmt java -jar "$KTFMT_JAR" $KTFMT_ARGS
