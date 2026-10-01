#!/usr/bin/env bash
# =====================================================================
# zest_pipeline.sh  —  Zest (JQF) + Defects4J, one bug / one budget / one run
#
#   fuzz on FIXED -> replay corpus -> JUnit (regression oracle) -> .tar.bz2
#   -> defects4j test on f and b -> run_bug_detection + run_coverage
#   -> append a row to the combined results and rebuild the reports
#
# Usage:
#   ./zest_pipeline.sh bugs/Lang_1.env 300 1
#   SKIP_FUZZ=1 ./zest_pipeline.sh bugs/Lang_1.env 300 1   # reuse existing corpus
#
# Combined results (all bugs, all runs) in ~/results/:
#   zest_summary.csv     one row per run
#   zest_aggregate.csv   mean / SD per bug x budget
#   zest_report.md       readable report of everything (rebuilt every run)
#   reports/<bug>-b<budget>-r<run>.md   per-run detail
# =====================================================================
set -uo pipefail

if [ $# -lt 3 ]; then
  echo "Usage: $0 <bugs/PID_BID.env> <budget_sec> <run_no>"; exit 1
fi
CONF="$1"; BUDGET="$2"; RUN="$3"
HERE=$(cd "$(dirname "$0")" && pwd)

# ---- bug config ----
# required: PID BID DRIVER METHOD
# writer  : WRITER=generic + TARGET_CLASS + TARGET_METHOD   (static method taking a String)
#       or: WRITER=generic + TARGET_METHODS=Cls#m,Cls#m      (multi-method driver, e.g. AutoFuzz)
#       or: WRITER=<custom class> + TEST_FILE
source "$CONF"
: "${JQF:=$HOME/jqf}"
: "${WORK:=$HOME/zest-d4j}"
: "${SKIP_FUZZ:=0}"
: "${WRITER:=generic}"
: "${MUTATION:=1}"          # MUTATION=0 skips run_mutation (slow)
export TZ=America/Los_Angeles

if [ "$WRITER" = "harness" ]; then
  : "${TEST_PKG:?set TEST_PKG in $CONF}"; : "${TEST_CLS:?set TEST_CLS in $CONF}"
  TEST_FILE=${TEST_PKG//.//}/$TEST_CLS.java
elif [ "$WRITER" = "generic" ]; then
  if [ -z "${TARGET_METHODS:-}" ]; then
    : "${TARGET_CLASS:?set TARGET_CLASS or TARGET_METHODS in $CONF}"; : "${TARGET_METHOD:?set TARGET_METHOD in $CONF}"
    TARGET_METHODS="$TARGET_CLASS#$TARGET_METHOD"
  fi
  FIRST=${TARGET_METHODS%%#*}; FIRST=${FIRST%%\$*}
  TEST_PKG=${FIRST%.*}
  TEST_CLS=${FIRST##*.}_Zest_Test
  TEST_FILE=${TEST_PKG//.//}/$TEST_CLS.java
fi
: "${TEST_FILE:?set TEST_FILE in $CONF}"

KEY=${PID}_${BID}
D4J_F=$HOME/d4j/${KEY}f
D4J_B=$HOME/d4j/${KEY}b
RES=$HOME/results
TID=$((BUDGET * 100 + RUN))                 # numeric test_id required by D4J
OUT=$WORK/out/$KEY/b${BUDGET}_r${RUN}
GEN=$WORK/gen/$KEY/b${BUDGET}_r${RUN}
BIN=$WORK/bin/$KEY
SUITE_DIR=$HOME/suites/$PID/zest/$BID/t$TID
SUITE=$SUITE_DIR/${PID}-${BID}f-zest.${TID}.tar.bz2
SUMMARY=$RES/zest_summary.csv
REPORT=$RES/reports/${PID}-${BID}-b${BUDGET}-r${RUN}.md
mkdir -p "$RES/reports" "$RES/bugdet" "$RES/cov" "$RES/mut" "$SUITE_DIR" "$GEN"

log() { echo "[$(date +%H:%M:%S)] $*"; }
die() { echo "ERROR: $*" >&2; exit 1; }
pct() { awk -v a="$1" -v b="$2" 'BEGIN{ if (b>0) printf "%.2f", 100*a/b; else printf "0" }'; }
T0=$(date +%s)

# ---------- 0) checkout + compile both versions ----------
for v in b f; do
  dir=$HOME/d4j/${KEY}$v
  if [ ! -d "$dir" ]; then
    log "checkout $PID-${BID}$v"
    timeout 600 defects4j checkout -p "$PID" -v "${BID}$v" -w "$dir" >/dev/null 2>&1 || die "checkout ${BID}$v"
  fi
  (cd "$dir" && timeout 900 defects4j compile >/dev/null 2>&1) || die "compile ${BID}$v"
done
CP_F=$(cd "$D4J_F" && defects4j export -p cp.compile 2>/dev/null)
JQF_CP=$("$JQF"/scripts/classpath.sh)

log "compile driver: src/common + src/$KEY"
[ -d "$WORK/src/$KEY" ] || die "no driver folder $WORK/src/$KEY (run new_bug.sh first)"
rm -rf "$BIN"; mkdir -p "$BIN"
javac -nowarn -d "$BIN" -cp "$CP_F:$JQF_CP" "$WORK"/src/common/*.java "$WORK/src/$KEY"/*.java \
  || die "javac failed"
RUN_CP="$BIN:$CP_F:$JQF_CP"

# ---------- 1) fuzz on fixed version ----------
if [ "$SKIP_FUZZ" != "1" ]; then
  rm -rf "$OUT"; mkdir -p "$(dirname "$OUT")"
  log "Zest fuzzing ${BUDGET}s  ($DRIVER#$METHOD)"
  FT0=$(date +%s)
  timeout -s INT -k 10 "$BUDGET" "$JQF"/bin/jqf-zest -c "$RUN_CP" "$DRIVER" "$METHOD" "$OUT" >/dev/null 2>&1
  pkill -f "ZestDriver.*$OUT" 2>/dev/null
  echo $(( $(date +%s) - FT0 )) > "$OUT/fuzz_time"
fi
[ -d "$OUT/corpus" ] || die "no corpus at $OUT"

# ---------- 2) replay corpus (+ failures) -> inputs.txt ----------
INPUTS=$GEN/inputs.txt; rm -f "$INPUTS"
N_FILES=$(ls "$OUT"/corpus "$OUT"/failures 2>/dev/null | grep -vc ':$')
log "replay $N_FILES corpus/failure files"
FILES=(); for f in "$OUT"/corpus/* "$OUT"/failures/*; do [ -f "$f" ] && FILES+=("$f"); done
# fast path: all files in one JVM
ZEST_DUMP=$INPUTS JQF_DISABLE_INSTRUMENTATION=1 timeout -s INT -k 5 600 \
  "$JQF"/bin/jqf-repro -c "$RUN_CP" "$DRIVER" "$METHOD" "${FILES[@]}" >/dev/null 2>&1
if [ "$(cat "$INPUTS" 2>/dev/null | wc -l)" -lt "${#FILES[@]}" ]; then
  # fallback: one JVM per file (slow but robust to hangs/crashes)
  rm -f "$INPUTS"
  for f in "${FILES[@]}"; do
    ZEST_DUMP=$INPUTS JQF_DISABLE_INSTRUMENTATION=1 timeout -s INT -k 5 30 \
      "$JQF"/bin/jqf-repro -c "$RUN_CP" "$DRIVER" "$METHOD" "$f" >/dev/null 2>&1
  done
fi
[ -s "$INPUTS" ] || die "no inputs replayed (does the driver call InputLog.record?)"

# ---------- 3) inputs -> JUnit -> tar.bz2 ----------
log "write JUnit tests ($WRITER)"
rm -rf "$GEN/src"; mkdir -p "$GEN/src/$(dirname "$TEST_FILE")"
if [ "$WRITER" = "generic" ]; then
  timeout 900 java -cp "$RUN_CP" zestd4j.GenericTestWriter "$INPUTS" "$GEN/src/$TEST_FILE" \
       "$TARGET_METHODS" "$TEST_PKG" "$TEST_CLS" || die "writer failed"
elif [ "$WRITER" = "harness" ]; then
  timeout 1800 java -cp "$RUN_CP" zestd4j.HarnessTestWriter "$INPUTS" "$GEN/src/$TEST_FILE" \
       "$TEST_PKG" "$TEST_CLS" "$WORK/src/$KEY/targets.txt" "$WORK/src/common/Harness.java" || die "writer failed"
else
  java -cp "$RUN_CP" "$WRITER" "$INPUTS" "$GEN/src/$TEST_FILE" || die "writer failed"
fi
NTESTS=$(grep -c "@Test" "$GEN/src/$TEST_FILE")
[ "$NTESTS" -gt 0 ] || die "no tests generated"
# total length = number of statements in the test suite (like EvoSuite "length")
TEST_LEN=$(awk '/ZEST-HARNESS/{exit} {print}' "$GEN/src/$TEST_FILE" | grep -vE '^\s*(package|import) ' | tr -cd ';' | wc -c)
rm -f "$SUITE"; tar -cjf "$SUITE" -C "$GEN/src" .
# remove tests that fail on the FIXED version (flaky / non-deterministic), like D4J does for EvoSuite/Randoop
FIXTS="$(dirname "$(command -v defects4j)")/../util/fix_test_suite.pl"
if [ -f "$FIXTS" ]; then
  log "fix_test_suite (remove flaky/broken tests)"
  timeout 2400 perl "$FIXTS" -p "$PID" -d "$SUITE_DIR" >/dev/null 2>&1
  TMPX=$(mktemp -d); tar -xjf "$SUITE" -C "$TMPX" 2>/dev/null
  N2=$(cat $(find "$TMPX" -name '*.java') 2>/dev/null | grep -c "@Test"); rm -rf "$TMPX"
  [ -n "$N2" ] && [ "$N2" -gt 0 ] && NTESTS=$N2
fi

# ---------- 4) run suite on fixed and buggy ----------
run_suite() { (cd "$1" && timeout 1200 defects4j test -s "$SUITE" 2>&1 | sed -n 's/^Failing tests: //p'); }
log "defects4j test  fixed / buggy"
FAIL_F=$(run_suite "$D4J_F"); cp "$D4J_F/failing_tests" "$GEN/failing_fixed.txt" 2>/dev/null
FAIL_B=$(run_suite "$D4J_B"); cp "$D4J_B/failing_tests" "$GEN/failing_buggy.txt" 2>/dev/null
FAIL_F=${FAIL_F:-NA}; FAIL_B=${FAIL_B:-NA}
[ "$FAIL_F" != "0" ] && log "WARNING: $FAIL_F test(s) fail on the FIXED version (flaky / bad oracle)"

# ---------- 5) D4J standard metrics ----------
log "run_bug_detection + run_coverage"
# drop stale rows of this suite so D4J recomputes (it never overwrites existing rows)
for rf in "$RES/bugdet/bug_detection" "$RES/cov/coverage" "$RES/mut/mutation"; do
  [ -f "$rf" ] && { grep -v "^$PID,${BID}f,zest,$TID," "$rf" > "$rf.tmp"; mv "$rf.tmp" "$rf"; }
done
timeout 1800 run_bug_detection.pl -p "$PID" -d "$SUITE_DIR" -o "$RES/bugdet" >/dev/null 2>&1
timeout 1800 run_coverage.pl      -p "$PID" -d "$SUITE_DIR" -o "$RES/cov"    >/dev/null 2>&1
BD=$(grep "^$PID,${BID}f,zest,$TID," "$RES/bugdet/bug_detection" 2>/dev/null | tail -1 | tr -d "\r")
CV=$(grep "^$PID,${BID}f,zest,$TID," "$RES/cov/coverage"        2>/dev/null | tail -1 | tr -d "\r")
BD_CLASS=$(echo "$BD" | cut -d, -f5); BD_TRIG=$(echo "$BD" | cut -d, -f6)
LT=$(echo "$CV" | cut -d, -f5); LC=$(echo "$CV" | cut -d, -f6)
BT=$(echo "$CV" | cut -d, -f7); BC=$(echo "$CV" | cut -d, -f8)
LT=${LT:-0}; LC=${LC:-0}; BT=${BT:-0}; BC=${BC:-0}
LPCT=$(pct "$LC" "$LT"); BPCT=$(pct "$BC" "$BT")
[ "$BD_CLASS" = "Fail" ] && DETECTED=yes || DETECTED=no

# mutation score (Major) = killed / (generated - excluded)
MG=0; MK=0; MPCT=NA
if [ "$MUTATION" = "1" ]; then
  log "run_mutation (may take a few minutes)"
  timeout "${MUTATION_TIMEOUT:-2400}" run_mutation.pl -p "$PID" -d "$SUITE_DIR" -o "$RES/mut" >/dev/null 2>&1
  MROW=$(awk -F, -v p="$PID" -v v="${BID}f" -v t="$TID" '
      { sub(/\r$/, "") } NR==1 { for (i=1;i<=NF;i++) c[$i]=i; next }
      $c["project_id"]==p && $c["version_id"]==v && $c["test_suite_source"]=="zest" && $c["test_id"]==t && $c["mut_generated"] ~ /^[0-9]+$/ {
        r=$c["mut_generated"]-$c["mut_excluded"] "," $c["mut_killed"] }
      END { print r }' "$RES/mut/mutation" 2>/dev/null)
  if [ -n "$MROW" ]; then MG=${MROW%,*}; MK=${MROW#*,}; MPCT=$(pct "$MK" "$MG"); fi
fi

# ---------- 6) Zest statistics (last row of plot_data) ----------
LAST=$(grep -v '^#' "$OUT/plot_data" | tail -1 | tr -d ' ')
CYCLES=$(echo "$LAST" | cut -d, -f2);  CORPUS=$(echo "$LAST" | cut -d, -f4)
VALID=$(echo "$LAST" | cut -d, -f12);  INVALID=$(echo "$LAST" | cut -d, -f13)
ZCOV=$(echo "$LAST" | cut -d, -f15);   ZVCOV=$(echo "$LAST" | cut -d, -f16)
EXECS=$(( ${VALID:-0} + ${INVALID:-0} )); VPCT=$(pct "${VALID:-0}" "$EXECS")
NFAIL_ZEST=$(ls "$OUT/failures" 2>/dev/null | wc -l)
if [ -f "$OUT/fuzz_time" ]; then FUZZ_TIME=$(cat "$OUT/fuzz_time")
else FUZZ_TIME=$(grep -v '^#' "$OUT/plot_data" | awk -F, 'NR==1{a=$1} {b=$1} END{print b-a+1}'); fi
ELAPSED=$(( $(date +%s) - T0 ))

# ---------- 7) append to combined summary (replace same bug/budget/run) ----------
HDR="date,project,bug,budget_s,run,test_id,execs,valid_pct,corpus,zest_cov_total,zest_cov_valid,zest_failures,num_tests,fail_on_fixed,fail_on_buggy,bug_detection,num_trigger,lines_total,lines_covered,line_cov_pct,branches_total,branches_covered,branch_cov_pct,fault_detected,fuzz_time_s,cycles,test_length,mutants,mutants_killed,mutation_pct"
if [ -f "$SUMMARY" ] && [ "$(head -1 "$SUMMARY")" != "$HDR" ]; then
  mv "$SUMMARY" "$SUMMARY.old.$(date +%s)"; log "old summary format backed up"
fi
[ -f "$SUMMARY" ] || echo "$HDR" > "$SUMMARY"
awk -F, -v p="$PID" -v b="$BID" -v g="$BUDGET" -v r="$RUN" \
  'NR==1 || !($2==p && $3==b && $4==g && $5==r)' "$SUMMARY" > "$SUMMARY.tmp" && mv "$SUMMARY.tmp" "$SUMMARY"
echo "$(date +%F),$PID,$BID,$BUDGET,$RUN,$TID,$EXECS,$VPCT,$CORPUS,$ZCOV,$ZVCOV,$NFAIL_ZEST,$NTESTS,$FAIL_F,$FAIL_B,${BD_CLASS:-NA},${BD_TRIG:-0},$LT,$LC,$LPCT,$BT,$BC,$BPCT,$DETECTED,$FUZZ_TIME,${CYCLES:-0},$TEST_LEN,$MG,$MK,$MPCT" >> "$SUMMARY"

# ---------- 8) per-run report ----------
{
  echo "# Zest – $PID-$BID (budget ${BUDGET}s, run $RUN)"
  echo
  echo "| Metric | Value |"
  echo "|---|---|"
  echo "| Driver | \`$DRIVER#$METHOD\` |"
  echo "| Executions | $EXECS (valid ${VPCT}%) |"
  echo "| Cycles | $CYCLES |"
  echo "| Corpus | $CORPUS |"
  echo "| Zest branch coverage (total / valid) | $ZCOV / $ZVCOV |"
  echo "| Zest failures (on fixed) | $NFAIL_ZEST |"
  echo "| JUnit tests | $NTESTS |"
  echo "| Failing on fixed | $FAIL_F |"
  echo "| Failing on buggy | $FAIL_B |"
  echo "| Bug detection | ${BD_CLASS:-NA} (triggering: ${BD_TRIG:-0}) |"
  echo "| **Fault detected** | **$DETECTED** |"
  echo "| Line coverage | $LC / $LT = ${LPCT}% |"
  echo "| Branch coverage | $BC / $BT = ${BPCT}% |"
  echo "| Test suite length (statements) | $TEST_LEN |"
  echo "| Mutation score | $MK / $MG = ${MPCT}% |"
  echo "| Fuzz time | ${FUZZ_TIME}s |"
  echo "| Pipeline time | ${ELAPSED}s |"
  echo
  echo "## Failing tests on buggy version"
  echo
  if [ -s "$GEN/failing_buggy.txt" ]; then
    grep '^--- ' "$GEN/failing_buggy.txt" | sed 's/^--- /- /'
    echo; echo '```'; head -40 "$GEN/failing_buggy.txt"; echo '```'
  else
    echo "(none)"
  fi
  echo
  echo "- Suite: \`$SUITE\`"
  echo "- Tests: \`$GEN/src/$TEST_FILE\`"
  echo "- Corpus: \`$OUT\`"
} > "$REPORT"

# ---------- 9) rebuild aggregate + combined report ----------
bash "$HERE/zest_report.sh"

log "done $PID-$BID b${BUDGET} r${RUN}: fault_detected=$DETECTED  line=${LPCT}%  branch=${BPCT}%  mutation=${MPCT}%  tests=$NTESTS"
echo "  results CSV     : $RES/zest_results.csv"
echo "  combined report : $RES/zest_report.md"
echo "  all runs (CSV)  : $SUMMARY"
echo "  this run        : $REPORT"
