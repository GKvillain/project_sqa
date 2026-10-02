# Zest – Time-8 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 373619 (valid 92.96%) |
| Cycles | 19 |
| Corpus | 209 |
| Zest branch coverage (total / valid) | 847 / 729 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 209 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 230 / 357 = 64.43% |
| Branch coverage | 103 / 182 = 56.59% |
| Test suite length (statements) | 209 |
| Mutation score | 345 / 537 = 64.25% |
| Fuzz time | 124s |
| Pipeline time | 258s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Time/zest/8/t12001/Time-8f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_8/b120_r1/src/org/joda/time/DateTimeZone_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_8/b120_r1`
