#!/bin/bash
# Sets up Claude Code on the web sessions so Gradle can build. Does nothing locally.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

# Maven Central rate-limits cloud sessions (429s), so resolve from Google's mirror of it first
INIT_DIR="$HOME/.gradle/init.d"
mkdir -p "$INIT_DIR"
cat > "$INIT_DIR/central-mirror.gradle.kts" <<'EOF'
val mirror = "https://maven-central.storage-download.googleapis.com/maven2/"
beforeSettings {
  pluginManagement.repositories { maven(mirror) { name = "CentralMirror" } }
  dependencyResolutionManagement.repositories { maven(mirror) { name = "CentralMirror" } }
}
EOF

# Android SDK for the Android targets and lint. AGP downloads the platform itself
# once the licences are accepted. Needs dl.google.com in the environment's allowed domains.
SDK_DIR="$HOME/android-sdk"
SDKMANAGER="$SDK_DIR/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMANAGER" ]; then
  TMP_ZIP="$(mktemp --suffix=.zip)"
  if curl -fsSL -o "$TMP_ZIP" \
    https://dl.google.com/android/repository/commandlinetools-linux-9862592_latest.zip; then
    mkdir -p "$SDK_DIR/cmdline-tools"
    rm -rf "$SDK_DIR/cmdline-tools/latest" "$SDK_DIR/cmdline-tools/cmdline-tools"
    unzip -q "$TMP_ZIP" -d "$SDK_DIR/cmdline-tools"
    mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
  else
    echo "Couldn't download the Android command line tools; is dl.google.com allowed?" >&2
  fi
  rm -f "$TMP_ZIP"
fi

if [ -x "$SDKMANAGER" ]; then
  yes | "$SDKMANAGER" --sdk_root="$SDK_DIR" --licenses > /dev/null 2>&1 || true
  if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
    echo "export ANDROID_HOME=\"$SDK_DIR\"" >> "$CLAUDE_ENV_FILE"
  fi
fi
