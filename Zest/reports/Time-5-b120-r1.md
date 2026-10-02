# Zest – Time-5 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 193199 (valid 97.09%) |
| Cycles | 24 |
| Corpus | 35 |
| Zest branch coverage (total / valid) | 485 / 482 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 35 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 38 / 288 = 13.19% |
| Branch coverage | 6 / 64 = 9.38% |
| Test suite length (statements) | 35 |
| Mutation score | 512 / 550 = 93.09% |
| Fuzz time | 125s |
| Pipeline time | 356s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Time/zest/5/t12001/Time-5f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Time_5/b120_r1/src/org/joda/time/Period_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Time_5/b120_r1`
