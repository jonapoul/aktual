# shellcheck shell=sh
# Shared git helpers for scripts that operate on files changed vs a base branch.
# Source this file (`. "$SCRIPT_DIR/lib/git.sh"`); don't execute it directly.
# Kept POSIX-compatible so both /bin/sh and bash callers can use it.

# Default base branch: origin/main when the remote branch exists, since a local main is easily
# stale, otherwise main.
git_default_base() {
  if git rev-parse --verify --quiet origin/main >/dev/null; then
    echo "origin/main"
  else
    echo "main"
  fi
}

# Resolve the merge-base of the given branch and HEAD. Fails with an error rather than guessing
# when the branch doesn't exist or shares no history with HEAD (e.g. a shallow clone).
git_merge_base() {
  if ! git rev-parse --verify --quiet "$1^{commit}" >/dev/null; then
    echo "Error: unknown base ref '$1'" >&2
    return 1
  fi
  if ! git merge-base "$1" HEAD; then
    echo "Error: no merge-base between '$1' and HEAD" >&2
    return 1
  fi
}

# Short form of a commit SHA, or "unknown" if it can't be resolved.
git_short_sha() {
  git rev-parse --short "$1" 2>/dev/null || echo "unknown"
}

# List paths changed (committed, uncommitted, and untracked) since the given ref, including
# deleted ones and both sides of a rename.
git_changed_paths() {
  {
    git -c core.quotePath=false diff --name-only --no-renames "$1"
    git -c core.quotePath=false ls-files --others --exclude-standard
  } | sort -u
}

# Like git_changed_paths, but only the paths that still exist on disk.
git_changed_files() {
  git_changed_paths "$1" | while IFS= read -r file; do
    if [ -f "$file" ]; then printf '%s\n' "$file"; fi
  done
}
