#!/usr/bin/env bash
# Rebuilds ~/results/zest_aggregate.csv and ~/results/zest_report.md
# from ~/results/zest_summary.csv (called automatically by zest_pipeline.sh).
set -uo pipefail
RES=${RES:-$HOME/results}
SUMMARY=$RES/zest_summary.csv
AGG=$RES/zest_aggregate.csv
MD=$RES/zest_report.md
CSV=$RES/zest_results.csv          # group format, one row per run
CSV_AVG=$RES/zest_results_avg.csv  # group format, mean per bug x budget
[ -f "$SUMMARY" ] || { echo "no $SUMMARY yet"; exit 0; }

# columns: 1 date 2 project 3 bug 4 budget 5 run 6 tid 7 execs 8 valid% 9 corpus 10 zcov 11 zvcov
# 12 zfail 13 tests 14 fail_f 15 fail_b 16 bd 17 trig 18 lt 19 lc 20 line% 21 bt 22 bc 23 branch% 24 detected
# 25 fuzz_time 26 cycles 27 test_length 28 mutants 29 killed 30 mutation%

# ---- group-format CSV (UTF-8 with BOM so Excel shows Thai headers) ----
GHDR='Project,เวลา (วิ),Generations,Statements,Fitness,Line (%),Branch (%),จำนวนเทสต์,ความยาวรวม,Mutation (%)'
{
  printf '\xEF\xBB\xBF%s\n' "$GHDR"
  awk -F, 'NR>1 {
      m = ($30=="NA" || $30=="") ? "NA" : sprintf("%.0f", $30)
      printf "%s-%sb,%s,%s,%s,%s,%.0f,%.0f,%s,%s,%s\n", $2,$3,$25,$26,$7,$10,$20,$23,$13,$27,m
    }' "$SUMMARY" | sort -t, -k1,1V -s
} > "$CSV"
{
  printf '\xEF\xBB\xBF%s\n' "Project,Budget (วิ),Runs,${GHDR#Project,}"
  awk -F, 'NR>1 {
      k=$2"-"$3"b,"$4; n[k]++
      t[k]+=$25; g[k]+=$26; st[k]+=$7; f[k]+=$10; l[k]+=$20; b[k]+=$23; nt[k]+=$13; len[k]+=$27
      if ($30!="NA" && $30!="") { m[k]+=$30; mn[k]++ }
    }
    END { for (k in n)
      printf "%s,%d,%.1f,%.1f,%.1f,%.1f,%.2f,%.2f,%.1f,%.1f,%s\n", k, n[k], t[k]/n[k], g[k]/n[k], st[k]/n[k], f[k]/n[k],
        l[k]/n[k], b[k]/n[k], nt[k]/n[k], len[k]/n[k], (mn[k] ? sprintf("%.2f", m[k]/mn[k]) : "NA") }' "$SUMMARY" \
    | sort -t, -k1,1V -k2,2n
} > "$CSV_AVG"

# ---- aggregate per project x bug x budget (mean, sample SD) ----
awk -F, 'NR>1 {
  k=$2","$3","$4; n[k]++
  l[k]+=$20; l2[k]+=$20*$20; b[k]+=$23; b2[k]+=$23*$23
  t[k]+=$13; c[k]+=$9; v[k]+=$8; d[k]+=($24=="yes")
  if ($30!="NA" && $30!="") { mu[k]+=$30; mn[k]++ }
}
function sd(s, s2, m,   x) { if (m < 2) return 0; x = (s2 - s*s/m) / (m-1); return x > 0 ? sqrt(x) : 0 }
END {
  print "project,bug,budget_s,runs,num_tests_mean,corpus_mean,valid_pct_mean,line_cov_mean,line_cov_sd,branch_cov_mean,branch_cov_sd,runs_detected,detection_rate,mutation_mean"
  for (k in n)
    printf "%s,%d,%.1f,%.1f,%.2f,%.2f,%.2f,%.2f,%.2f,%d,%.2f,%s\n", k, n[k], t[k]/n[k], c[k]/n[k], v[k]/n[k],
      l[k]/n[k], sd(l[k],l2[k],n[k]), b[k]/n[k], sd(b[k],b2[k],n[k]), d[k], d[k]/n[k], (mn[k] ? sprintf("%.2f", mu[k]/mn[k]) : "NA")
}' "$SUMMARY" | { read -r h; echo "$h"; sort -t, -k1,1 -k2,2n -k3,3n; } > "$AGG"

# ---- markdown report ----
{
  echo "# Zest (JQF) on Defects4J — combined results"
  echo
  echo "_Updated: $(date '+%Y-%m-%d %H:%M')_"
  echo
  echo "## Overall"
  echo
  awk -F, 'NR>1 {
      bug=$2"-"$3; seen[bug]=1; runs++; if ($24=="yes") { det[bug]=1; rd++ }
      lsum+=$20; bsum+=$23
    }
    END {
      nb=0; nd=0; for (x in seen) { nb++; if (x in det) nd++ }
      print "| Metric | Value |"; print "|---|---|"
      printf "| Bugs tested | %d |\n", nb
      printf "| Bugs detected (at least 1 run) | %d |\n", nd
      printf "| **Fault detection rate (bug level)** | **%.2f%%** (%d/%d) |\n", (nb?100*nd/nb:0), nd, nb
      printf "| Runs | %d |\n", runs
      printf "| Fault detection rate (run level) | %.2f%% (%d/%d) |\n", (runs?100*rd/runs:0), rd, runs
      printf "| Mean line coverage (all runs) | %.2f%% |\n", (runs?lsum/runs:0)
      printf "| Mean branch coverage (all runs) | %.2f%% |\n", (runs?bsum/runs:0)
    }' "$SUMMARY"
  echo
  echo "## Per bug × budget (mean ± SD over runs)"
  echo
  echo "| Bug | Budget (s) | Runs | Tests | Corpus | Valid % | Line cov % | Branch cov % | Mutation % | Detected |"
  echo "|---|---|---|---|---|---|---|---|---|---|"
  awk -F, 'NR>1 { printf "| %s-%s | %s | %s | %s | %s | %s | %s ± %s | %s ± %s | %s | %s/%s |\n",
      $1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$14,$12,$4 }' "$AGG"
  echo
  echo "## All runs"
  echo
  echo "| Date | Bug | Budget (s) | Run | Execs | Valid % | Corpus | Zest cov (all/valid) | Tests | Fail f | Fail b | Bug detection | Line cov % | Branch cov % | Mutation % | Detected |"
  echo "|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|"
  awk -F, 'NR>1 { printf "| %s | %s-%s | %s | %s | %s | %s | %s | %s/%s | %s | %s | %s | %s (%s) | %s (%s/%s) | %s (%s/%s) | %s | %s |\n",
      $1,$2,$3,$4,$5,$7,$8,$9,$10,$11,$13,$14,$15,$16,$17,$20,$19,$18,$23,$22,$21,$30,$24 }' "$SUMMARY" \
    | sort -t'|' -k3,3 -k4,4n -k5,5n
  echo
  echo "Coverage = lines/branches of the modified class(es), measured by Defects4J \`run_coverage\` on the fixed version."
  echo "Detected = \`run_bug_detection\` classifies the suite as **Fail** (at least one test passes on fixed and fails on buggy)."
  echo "Per-run details: \`reports/\`. Raw data: \`zest_summary.csv\`, \`zest_aggregate.csv\`. Group-format CSV: \`zest_results.csv\`, \`zest_results_avg.csv\`."
} > "$MD"
