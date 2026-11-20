#!/usr/bin/env bash
#
# Shell for workflow run steps: keeps a failing step's output in $RUNNER_TEMP/failed-step.log so
# pr_comment.py can show it in the PR comment. Use with `defaults.run.shell: bash <this> {0}`.

set -o pipefail

log="$RUNNER_TEMP/failed-step.log"
bash --noprofile --norc -eo pipefail "$1" 2>&1 | tee "$log"
status=$?
if [[ $status -eq 0 ]]; then
  rm -f "$log"
fi
exit $status
