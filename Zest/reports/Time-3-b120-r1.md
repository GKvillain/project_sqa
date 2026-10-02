# Zest – Time-3 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 346345 (valid 98.46%) |
| Cycles | 14 |
| Corpus | 225 |
| Zest branch coverage (total / valid) | 971 / 969 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 278 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 5 / 248 = 2.02% |
| Branch coverage | 1 / 63 = 1.59% |
| Test suite length (statements) | 140 |
| Mutation score | 162 / 162 = 100.00% |
| Fuzz time | 124s |
| Pipeline time | 240s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Time/zest/3/t12001/Time-3f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_3/b120_r1/src/org/joda/time/MutableDateTime_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_3/b120_r1`
