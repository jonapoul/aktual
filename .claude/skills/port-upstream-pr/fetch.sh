#!/bin/bash
# Fetches an upstream Actual PR into a blobless clone at build/upstream-actual and prints a summary.
# The full diff lands in build/upstream-actual/pr-<n>.diff, and the PR head is checked out as the
# local branch pr-<n> (its fork point from master as pr-<n>-base) so files can be read at either
# revision with `git show pr-<n>:<path>`.
# Usage: .claude/skills/port-upstream-pr/fetch.sh <pr-number-or-url>
set -euo pipefail

if [ $# -ne 1 ]; then
  echo "Usage: $0 <pr-number-or-url>" >&2
  exit 1
fi

PR="$(echo "$1" | sed -E 's#.*/pull/([0-9]+).*#\1#')"
if ! [[ "$PR" =~ ^[0-9]+$ ]]; then
  echo "Not a PR number or URL: $1" >&2
  exit 1
fi

ROOT="$(git rev-parse --show-toplevel)"
DIR="$ROOT/build/upstream-actual"
UPSTREAM="https://github.com/actualbudget/actual"

if [ ! -d "$DIR/.git" ]; then
  mkdir -p "$(dirname "$DIR")"
  git clone -q --filter=blob:none --no-checkout "$UPSTREAM" "$DIR"
fi

git -C "$DIR" fetch -q origin master
git -C "$DIR" fetch -q -f origin "pull/$PR/head:pr-$PR"

git -C "$DIR" branch -q -f "pr-$PR-base" "$(git -C "$DIR" merge-base origin/master "pr-$PR")"
BASE="pr-$PR-base..pr-$PR"
git -C "$DIR" diff "$BASE" > "$DIR/pr-$PR.diff"

echo "== PR #$PR: $UPSTREAM/pull/$PR"

# Metadata via gh where it can reach upstream (local machines). Cloud sessions fall back to git.
if gh pr view "$PR" --repo actualbudget/actual --json title,state,author,body \
  -q '"Title: \(.title)\nState: \(.state)\nAuthor: \(.author.login)\n\n\(.body)"' 2>/dev/null; then
  :
else
  SQUASH="$(git -C "$DIR" log origin/master --format='%h %s' --grep="(#$PR)\$" -1)"
  if [ -n "$SQUASH" ]; then
    echo "State: MERGED as $SQUASH"
  else
    echo "State: not merged into master (open, closed or merged under another message)"
  fi
  echo "Head commit: $(git -C "$DIR" log -1 --format='%h %s' "pr-$PR")"
fi

NOTES="upcoming-release-notes/$PR.md"
if git -C "$DIR" cat-file -e "pr-$PR:$NOTES" 2>/dev/null; then
  echo
  echo "== $NOTES"
  git -C "$DIR" show "pr-$PR:$NOTES"
fi

echo
echo "== Files"
git -C "$DIR" diff --stat=200 "$BASE"
echo
echo "Full diff: $DIR/pr-$PR.diff"
echo "Read a file at the PR head: git -C $DIR show pr-$PR:<path>"
echo "Read a file before the PR:  git -C $DIR show pr-$PR-base:<path>"
