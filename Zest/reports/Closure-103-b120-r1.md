# Zest – Closure-103 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 535659 (valid 55.82%) |
| Cycles | 57 |
| Corpus | 24 |
| Zest branch coverage (total / valid) | 166 / 162 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 24 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 22 / 765 = 2.88% |
| Branch coverage | 1 / 522 = 0.19% |
| Test suite length (statements) | 24 |
| Mutation score | 689 / 695 = 99.14% |
| Fuzz time | 120s |
| Pipeline time | 203s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/103/t12001/Closure-103f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_103/b120_r1/src/com/google/javascript/jscomp/ControlFlowAnalysis_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_103/b120_r1`
