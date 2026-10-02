# Zest – Time-6 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 52733 (valid 47.29%) |
| Cycles | 1 |
| Corpus | 203 |
| Zest branch coverage (total / valid) | 1215 / 1189 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 203 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 164 / 380 = 43.16% |
| Branch coverage | 39 / 182 = 21.43% |
| Test suite length (statements) | 203 |
| Mutation score | 400 / 479 = 83.51% |
| Fuzz time | 125s |
| Pipeline time | 263s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Time/zest/6/t12001/Time-6f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_6/b120_r1/src/org/joda/time/chrono/GJChronology_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_6/b120_r1`
