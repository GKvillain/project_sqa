# Zest – Time-4 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 250802 (valid 92.09%) |
| Cycles | 20 |
| Corpus | 109 |
| Zest branch coverage (total / valid) | 771 / 768 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 109 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 44 / 253 = 17.39% |
| Branch coverage | 11 / 106 = 10.38% |
| Test suite length (statements) | 178 |
| Mutation score | 348 / 371 = 93.80% |
| Fuzz time | 124s |
| Pipeline time | 235s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Time/zest/4/t12001/Time-4f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_4/b120_r1/src/org/joda/time/Partial_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_4/b120_r1`
