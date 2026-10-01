#!/usr/bin/env bash
# Split ~/results CSVs into one file per project (e.g. zest_summary_Lang.csv, zest_summary_Chart.csv)
RES=${RES:-$HOME/results}
for P in $(awk -F, 'NR>1{print $2}' "$RES/zest_summary.csv" | sort -u); do
  awk -F, -v p="$P" 'NR==1 || $2==p' "$RES/zest_summary.csv"   > "$RES/zest_summary_$P.csv"
  awk -F, -v p="$P" 'NR==1 || $1==p' "$RES/zest_aggregate.csv" > "$RES/zest_aggregate_$P.csv"
  for f in zest_results zest_results_avg; do
    [ -f "$RES/$f.csv" ] && awk -v p="$P-" 'NR==1 || index($0,p)==1' "$RES/$f.csv" > "$RES/${f}_$P.csv"
  done
  echo "$P: $(( $(wc -l < "$RES/zest_summary_$P.csv") - 1 )) runs"
done
