#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

docker run --rm \
  -v "$REPO_ROOT:/repo:ro" \
  -e GIT_CONFIG_COUNT=1 \
  -e GIT_CONFIG_KEY_0=safe.directory \
  -e GIT_CONFIG_VALUE_0=/repo \
  ghcr.io/betterleaks/betterleaks:v1.10.0@sha256:c6dfa75145fe5b31fdaf02feff18a73f1e11df8ca04876522f87f857656c83ca \
  git -v --config=/repo/config/betterleaks.toml /repo "$@"
