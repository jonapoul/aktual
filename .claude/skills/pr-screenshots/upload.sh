#!/bin/bash
# Hosts PNGs for a PR description without leaving them in the branch. Commits them, commits their
# removal, pushes both, then prints markdown linking to the images at the first commit's SHA.
# Usage: .claude/skills/pr-screenshots/upload.sh <png>...
set -euo pipefail

if [ $# -eq 0 ]; then
  echo "Usage: $0 <png>..." >&2
  exit 1
fi

cd "$(git rev-parse --show-toplevel)"

if [ -n "$(git status --porcelain)" ]; then
  echo "Working tree isn't clean. Commit or stash your changes first." >&2
  exit 1
fi

BRANCH="$(git branch --show-current)"
if [ "$BRANCH" = "main" ]; then
  echo "On main. Switch to the PR branch first." >&2
  exit 1
fi

# Works for plain GitHub remotes and the local git proxy used by cloud sessions
SLUG="$(git remote get-url origin | sed -E 's#\.git$##; s#.*[:/]([^/]+/[^/]+)$#\1#')"
DIR=".github/pr-screenshots/$(echo "$BRANCH" | tr '/' '-')"

mkdir -p "$DIR"
for f in "$@"; do
  cp "$f" "$DIR/"
done

git add "$DIR"
git commit -q -m "Add PR screenshots"
SHA="$(git rev-parse HEAD)"
git rm -q -r "$DIR"
git commit -q -m "Remove PR screenshots"
git push -q -u origin "$BRANCH"

for f in "$@"; do
  name="$(basename "$f")"
  echo "![${name%.png}](https://github.com/$SLUG/raw/$SHA/$DIR/$name)"
done
