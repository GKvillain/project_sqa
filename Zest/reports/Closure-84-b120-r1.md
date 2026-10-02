# Zest – Closure-84 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 365540 (valid 80.78%) |
| Cycles | 7 |
| Corpus | 152 |
| Zest branch coverage (total / valid) | 283 / 280 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 152 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 32 / 568 = 5.63% |
| Branch coverage | 1 / 305 = 0.33% |
| Test suite length (statements) | 152 |
| Mutation score | 334 / 340 = 98.24% |
| Fuzz time | 120s |
| Pipeline time | 248s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/84/t12001/Closure-84f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_84/b120_r1/src/com/google/javascript/jscomp/parsing/IRFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_84/b120_r1`
