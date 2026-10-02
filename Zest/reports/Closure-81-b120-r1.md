# Zest – Closure-81 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 338652 (valid 83.19%) |
| Cycles | 6 |
| Corpus | 150 |
| Zest branch coverage (total / valid) | 286 / 283 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 150 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 31 / 592 = 5.24% |
| Branch coverage | 1 / 321 = 0.31% |
| Test suite length (statements) | 150 |
| Mutation score | 373 / 378 = 98.68% |
| Fuzz time | 120s |
| Pipeline time | 242s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/81/t12001/Closure-81f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_81/b120_r1/src/com/google/javascript/jscomp/parsing/IRFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_81/b120_r1`
