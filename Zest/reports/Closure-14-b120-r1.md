# Zest – Closure-14 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 453724 (valid 51.87%) |
| Cycles | 39 |
| Corpus | 30 |
| Zest branch coverage (total / valid) | 167 / 163 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 30 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 24 / 407 = 5.90% |
| Branch coverage | 1 / 276 = 0.36% |
| Test suite length (statements) | 30 |
| Mutation score | 351 / 365 = 96.16% |
| Fuzz time | 124s |
| Pipeline time | 274s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Closure/zest/14/t12001/Closure-14f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Closure_14/b120_r1/src/com/google/javascript/jscomp/ControlFlowAnalysis_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Closure_14/b120_r1`
