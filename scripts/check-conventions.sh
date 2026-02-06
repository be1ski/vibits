#!/bin/bash
# Checks package placement conventions defined in CLAUDE.md.
# Run as part of CI to catch misplaced code early.

set -euo pipefail

errors=0

# 1. @Composable functions must be in *.ui.* or *.view.* packages
echo "Checking: @Composable outside ui/view packages..."
composable_violations=$(
  grep -rn "@Composable" --include="*.kt" \
    core/ feature/ \
    | grep "/src/commonMain/" \
    | grep -v "/ui/" \
    | grep -v "/view/" \
    | grep -v "/test/" \
    | grep -v "/testing/" \
    | grep -v "/build/" \
    || true
)
if [ -n "$composable_violations" ]; then
  echo "ERROR: @Composable found outside *.ui.* or *.view.* packages:"
  echo "$composable_violations"
  errors=$((errors + 1))
fi

# 2. expect/actual declarations must be in *.platform.* or *.room.* packages
echo "Checking: expect declarations outside platform/room packages..."
expect_violations=$(
  grep -rn "^expect \|^expect$" --include="*.kt" \
    core/ feature/ \
    | grep "/src/commonMain/" \
    | grep -v "/platform/" \
    | grep -v "/room/" \
    | grep -v "/test/" \
    | grep -v "/testing/" \
    | grep -v "/build/" \
    || true
)
if [ -n "$expect_violations" ]; then
  echo "ERROR: expect declarations found outside *.platform.* or *.room.* packages:"
  echo "$expect_violations"
  errors=$((errors + 1))
fi

# 3. No Fake* classes in production commonMain (except testing modules)
echo "Checking: Fake* in production commonMain..."
fake_violations=$(
  grep -rn "^class Fake\|^object Fake\|^open class Fake\|^internal class Fake" --include="*.kt" \
    core/ feature/ \
    | grep "/src/commonMain/" \
    | grep -v "/testing/" \
    | grep -v "/test/" \
    | grep -v "/build/" \
    || true
)
if [ -n "$fake_violations" ]; then
  echo "ERROR: Fake* classes found in production commonMain (should be in testing/ or commonTest/):"
  echo "$fake_violations"
  errors=$((errors + 1))
fi

if [ "$errors" -gt 0 ]; then
  echo ""
  echo "Convention check FAILED with $errors violation(s)."
  exit 1
fi

echo "All convention checks passed."
