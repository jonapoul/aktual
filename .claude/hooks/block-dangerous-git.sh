#!/bin/bash
# Block destructive git commands that are hard or impossible to reverse.

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

if [ -z "$COMMAND" ]; then
  exit 0
fi

# Prefer rebase over merge
if echo "$COMMAND" | grep -qE '\bgit\s+merge\b'; then
  echo "Blocked: git merge is not allowed. Use 'git rebase <target-branch>' instead." >&2
  exit 2
fi

# Never force-push to main
if echo "$COMMAND" | grep -qE '\bgit\s+push\s+.*(--force|-f)\b.*\s*(main|master)\b|\bgit\s+push\s+(--force|-f)\s+(origin\s+)?(main|master)\b'; then
  echo "Blocked: force push to main is not allowed." >&2
  exit 2
fi

# Hard reset discards local changes
if echo "$COMMAND" | grep -qE '\bgit\s+reset\s+--hard\b'; then
  echo "Blocked: git reset --hard discards all local changes permanently. Ask the user for explicit confirmation first." >&2
  exit 2
fi

# git clean -f removes untracked files
if echo "$COMMAND" | grep -qE '\bgit\s+clean\s+-[a-zA-Z]*f[a-zA-Z]*'; then
  echo "Blocked: git clean -f removes untracked files permanently. Ask the user for explicit confirmation first." >&2
  exit 2
fi

# checkout . / restore . discards all changes
if echo "$COMMAND" | grep -qE '\bgit\s+(checkout|restore)\s+\.\s*$'; then
  echo "Blocked: this command discards all uncommitted changes. Ask the user for explicit confirmation first." >&2
  exit 2
fi

exit 0
