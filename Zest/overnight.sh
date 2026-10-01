#!/usr/bin/env bash
# =====================================================================
# overnight.sh — run Zest on EVERY bug of the given projects, unattended.
#
#   ./overnight.sh                               # all Lang bugs, 120s, 2 runs
#   PROJECTS="Lang Cli" ./overnight.sh
#   BUDGET=300 RUNS=3 ./overnight.sh
#   BUGS="1-20" ./overnight.sh                   # only bug ids 1..20
#
# Start it in the background so it survives closing the terminal:
#   nohup ./overnight.sh > ~/results/overnight.log 2>&1 &
#   tail -f ~/results/overnight.log              # watch (Ctrl+C only stops watching)
#
# - Resumable: bug/budget/run already in zest_summary.csv are skipped.
# - Loops runs on the OUTSIDE: every bug gets run 1 before any bug gets run 2.
# - Bugs with a hand-written bugs/<PID>_<BID>.env use it; others get an auto driver.
# - Bugs that cannot be fuzzed / fail are listed in ~/results/zest_skipped.csv.
# =====================================================================
set -uo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
: "${PROJECTS:=Lang}"
: "${BUDGET:=120}"
: "${RUNS:=2}"
: "${BUGS:=all}"
RES=$HOME/results; mkdir -p "$RES"
SUMMARY=$RES/zest_summary.csv
SKIPPED=$RES/zest_skipped.csv
[ -f "$SKIPPED" ] || echo "project,bug,run,stage,reason,time" > "$SKIPPED"
export TZ=America/Los_Angeles

log() { echo "[$(date '+%m-%d %H:%M:%S')] $*"; }
D4J_HOME=$(cd "$(dirname "$(command -v defects4j)")/../.." && pwd)

bug_ids() {   # active bug ids of a project
  local pid=$1 ids
  ids=$(defects4j bids -p "$pid" 2>/dev/null)
  [ -n "$ids" ] || ids=$(tail -n +2 "$D4J_HOME/framework/projects/$pid/active-bugs.csv" 2>/dev/null | cut -d, -f1)
  if [ "$BUGS" != "all" ]; then
    local lo=${BUGS%-*} hi=${BUGS#*-}
    ids=$(for i in $ids; do [ "$i" -ge "$lo" ] && [ "$i" -le "$hi" ] && echo "$i"; done)
  fi
  echo "$ids" | sort -n
}
done_already() { [ -f "$SUMMARY" ] && grep -q "^[^,]*,$1,$2,$BUDGET,$3," "$SUMMARY"; }
skipped_forever() { grep -q "^$1,$2,[^,]*,driver," "$SKIPPED" 2>/dev/null; }
skip() { echo "$1,$2,$3,$4,\"$5\",$(date '+%F %T')" >> "$SKIPPED"; log "  SKIP $1-$2: $5"; }

log "start: projects=[$PROJECTS] budget=${BUDGET}s runs=$RUNS bugs=$BUGS"
T0=$(date +%s)
for run in $(seq 1 "$RUNS"); do
  for pid in $PROJECTS; do
    IDS=$(bug_ids "$pid")
    [ -n "$IDS" ] || { log "no bugs found for $pid"; continue; }
    N=$(echo "$IDS" | wc -l); k=0
    for bid in $IDS; do
      k=$((k+1)); key=${pid}_${bid}
      done_already "$pid" "$bid" "$run" && continue
      skipped_forever "$pid" "$bid" && continue
      log "=== run $run/$RUNS  $pid-$bid  ($k/$N)  elapsed $(( ($(date +%s)-T0)/60 )) min ==="

      if [ ! -f "$HERE/bugs/$key.env" ]; then
        out=$("$HERE/auto_bug.sh" "$pid" "$bid" 2>&1); rc=$?
        if [ $rc -eq 2 ]; then skip "$pid" "$bid" "$run" driver "no public static method with one String parameter in modified class"; continue
        elif [ $rc -ne 0 ]; then skip "$pid" "$bid" "$run" setup "$(echo "$out" | tail -1 | tr -d '",')"; continue; fi
        log "  auto driver: $(echo "$out" | tail -1)"
      fi

      out=$("$HERE/zest_pipeline.sh" "$HERE/bugs/$key.env" "$BUDGET" "$run" 2>&1); rc=$?
      echo "$out" | grep -E "done |WARNING|ERROR" | sed 's/^/  /'
      if [ $rc -ne 0 ]; then
        skip "$pid" "$bid" "$run" pipeline "$(echo "$out" | grep -m1 ERROR | tr -d '",')"
        # a driver that does not compile will never work: stop retrying it
        echo "$out" | grep -q "javac failed" && skip "$pid" "$bid" "$run" driver "driver does not compile"
      fi
      # free disk: generated sources/corpus stay, temp dirs of D4J scripts are removed
      rm -rf /tmp/run_bug_detection.pl_* /tmp/run_coverage.pl_* /tmp/run_mutation.pl_* 2>/dev/null
    done
  done
done
bash "$HERE/zest_report.sh"
log "ALL DONE in $(( ($(date +%s)-T0)/60 )) min"
log "results: $RES/zest_results.csv  |  report: $RES/zest_report.md  |  skipped: $SKIPPED"
