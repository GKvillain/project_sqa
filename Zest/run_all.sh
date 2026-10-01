#!/usr/bin/env bash
# Run several budgets x runs for one or more bugs.
# Usage:  ./run_all.sh "60 300 900" 5 bugs/Lang_1.env [bugs/Csv_4.env ...]
#         ./run_all.sh "300" 3 bugs/*.env
set -uo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
[ $# -lt 3 ] && { echo "Usage: $0 \"<budgets>\" <runs> <bug.env>..."; exit 1; }
BUDGETS=$1; RUNS=$2; shift 2
for conf in "$@"; do
  for b in $BUDGETS; do
    for r in $(seq 1 "$RUNS"); do
      echo "=================== $(basename "$conf" .env)  budget=${b}s  run=$r ==================="
      "$HERE/zest_pipeline.sh" "$conf" "$b" "$r" || echo "!! failed: $conf b=$b r=$r (continuing)"
    done
  done
done
echo; echo "Combined report: $HOME/results/zest_report.md"
