#!/bin/sh

ktlint '**/*.kt' '**/*.kts' '!**/build/**' --color --format
