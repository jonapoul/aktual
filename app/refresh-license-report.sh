#!/bin/bash

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd $SCRIPT_DIR/..

# Generate the report
./gradlew -q app:licenseReleaseReport --no-configuration-cache --rerun-tasks

# Create a temp file. Necessary because piping directly from jq into its own source files results in the file being
# totally empty
LICENSE_FILE="$SCRIPT_DIR/src/main/assets/open_source_licenses.json"
TEMP_FILE="$SCRIPT_DIR/src/main/assets/open_source_licenses_temp.json"
touch $TEMP_FILE

# Pretty print the file
jq 'sort_by(.dependency)' $LICENSE_FILE > $TEMP_FILE

# Delete the temporary file
mv $TEMP_FILE $LICENSE_FILE
rm -f $TEMP_FILE

echo "Pretty printed $LICENSE_FILE"
