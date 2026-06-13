# shellcheck shell=sh
# Shared git helpers for scripts that operate on files changed vs a base branch.
# Source this file (`. "$SCRIPT_DIR/lib/git.sh"`); don't execute it directly.
# Kept POSIX-compatible so both /bin/sh and bash callers can use it.

# Resolve the merge-base of the given branch and HEAD, falling back to HEAD.
git_merge_base() {
  git merge-base "$1" HEAD 2>/dev/null || git rev-parse HEAD
}

# Short form of a commit SHA, or "unknown" if it can't be resolved.
git_short_sha() {
  git rev-parse --short "$1" 2>/dev/null || echo "unknown"
}

# List existing files changed (committed, uncommitted, and untracked) since the given ref.
git_changed_files() {
  {
    git diff --name-only "$1" 2>/dev/null
    git ls-files --others --exclude-standard 2>/dev/null
  } | sort -u | while IFS= read -r file; do
    if [ -f "$file" ]; then printf '%s\n' "$file"; fi
  done
}
