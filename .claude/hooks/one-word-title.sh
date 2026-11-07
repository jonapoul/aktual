#!/usr/bin/env bash
# UserPromptSubmit hook: sets a one-word session title from the first prompt
[ -n "$ONE_WORD_TITLE_RUNNING" ] && exit 0

input=$(cat)
[ -n "$(jq -r '.session_title // empty' <<<"$input")" ] && exit 0

prompt=$(jq -r '.prompt // empty' <<<"$input" | head -c 2000)
[ -z "$prompt" ] && exit 0

word=$(ONE_WORD_TITLE_RUNNING=1 claude -p \
  --model haiku \
  --no-session-persistence \
  --settings '{"disableAllHooks":true}' \
  "Give a one-word title for the coding task below. Use the main noun it is about, never a verb.
Examples: 'fix the crash when opening a budget offline' -> Offline. 'make the transactions list scroll faster' -> Transactions. 'add a dark mode toggle' -> Theme.
Reply with only the word.

<task>
$prompt
</task>" 2>/dev/null | tr -cd '[:alnum:]-' | head -c 24 | sed 's/.*/\L\u&/')

[ -z "$word" ] && exit 0

jq -nc --arg t "$word" '{hookSpecificOutput: {hookEventName: "UserPromptSubmit", sessionTitle: $t}}'
