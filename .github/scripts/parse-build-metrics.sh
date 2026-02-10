#!/usr/bin/env bash
#
# Parses a Kotlin build report and outputs customSmallerIsBetter JSON
# for github-action-benchmark.
#
# Usage: parse-build-metrics.sh <report-file> <wall-clock-seconds>

set -euo pipefail

REPORT="$1"
WALL_CLOCK="$2"

if [[ ! -f "$REPORT" ]]; then
  echo "Error: report file not found: $REPORT" >&2
  exit 1
fi

# --- Extract metrics ---

# Total Kotlin compilation time (first match)
kotlin_time=$(grep -m1 '^Total time for Kotlin tasks:' "$REPORT" \
  | sed 's/Total time for Kotlin tasks: \([0-9.]*\) s.*/\1/' || true)

# Lines of code analyzed (first occurrence = aggregate)
lines_analyzed=$(grep -m1 'Number of lines analyzed:' "$REPORT" \
  | sed 's/.*Number of lines analyzed: \([0-9]*\)/\1/' || true)

# Top-5 heaviest compilation tasks (from the pipe-delimited table)
# Format: "3.314 s|3.8 %           |:feature:habits:presentation:compileKotlinDesktop"
compile_lines=$(grep -E '^[0-9]+\.[0-9]+ s\|' "$REPORT" | grep -v 'desktopTest' | head -5 || true)

# Top-3 heaviest test tasks
# Format: "Task ':feature:homescreen:desktopTest' finished in 13.411 s"
test_lines=$(grep -E "Task '.*:desktopTest' finished in" "$REPORT" | head -3 || true)

# --- Build JSON ---

json='['

# Wall clock time
json+=$(printf '{"name":"Wall clock time","unit":"s","value":%s}' "$WALL_CLOCK")

# Total Kotlin compilation time
if [[ -n "$kotlin_time" ]]; then
  json+=$(printf ',{"name":"Kotlin compilation time","unit":"s","value":%s}' "$kotlin_time")
fi

# Lines of code analyzed
if [[ -n "$lines_analyzed" ]]; then
  json+=$(printf ',{"name":"Lines of code analyzed","unit":"lines","value":%s}' "$lines_analyzed")
fi

# Top-5 compilation tasks
if [[ -n "$compile_lines" ]]; then
  while IFS='|' read -r time_col _pct_col task_col; do
    time_val=$(echo "$time_col" | sed 's/ *s$//')
    task_name=$(echo "$task_col" | xargs)
    json+=$(printf ',{"name":"Compile %s","unit":"s","value":%s}' "$task_name" "$time_val")
  done <<< "$compile_lines"
fi

# Top-3 test tasks
if [[ -n "$test_lines" ]]; then
  while IFS= read -r line; do
    time_val=$(echo "$line" | sed "s/.*finished in \([0-9.]*\) s/\1/")
    task_name=$(echo "$line" | sed "s/Task '\(.*\)' finished in.*/\1/")
    json+=$(printf ',{"name":"Test %s","unit":"s","value":%s}' "$task_name" "$time_val")
  done <<< "$test_lines"
fi

json+=']'

echo "$json"
