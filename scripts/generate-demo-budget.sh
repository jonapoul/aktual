#!/bin/sh
# Regenerates the bundled demo budget with upstream's own generator (the one behind
# demo.actualbudget.org), then records today as its generation date so the app can shift its dates.
# Needs node + npm. Pass an @actual-app/api version to pin one, defaults to latest.
set -eu

SCRIPT_DIR="$(dirname "$0")"
cd "$SCRIPT_DIR/.." || exit
ROOT="$(pwd)"

API_VERSION="${1:-latest}"
DB_FILE="$ROOT/aktual-budget/demo/src/commonMain/composeResources/files/demo-budget.sqlite"
DATES_FILE="$ROOT/aktual-budget/demo/src/commonMain/kotlin/aktual/budget/demo/DemoDates.kt"

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT
cd "$WORK_DIR"

npm init -y > /dev/null
npm install --silent "@actual-app/api@$API_VERSION"
mkdir data

cat > generate.mjs <<'JS'
import * as api from '@actual-app/api';
const internal = await api.init({ dataDir: './data' });
await internal.send('create-demo-budget');
await api.shutdown();
JS
node generate.mjs > /dev/null

cp data/_demo-budget/db.sqlite "$DB_FILE"

YEAR=$(date +%Y)
MONTH=$(date +%-m)
DAY=$(date +%-d)
sed -i.bak "s/^internal val DEMO_GENERATED_ON = LocalDate(.*)$/internal val DEMO_GENERATED_ON = LocalDate($YEAR, $MONTH, $DAY)/" "$DATES_FILE"
rm "$DATES_FILE.bak"

echo "Generated demo budget with @actual-app/api@$API_VERSION on $YEAR-$MONTH-$DAY"
echo "Check .github/upstream-migration-tracker/last-known-migration.txt still matches the API version"
